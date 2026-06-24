import { useEffect, useRef, useState } from "react";
import styles from "./StompChatPage.module.css";
import useAuthStore from "../utils/useAuthStore";
import { useNavigate, useParams } from "react-router-dom";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import axios from "axios";
import Nickname from "../commons/Nickname";

const StompChatPage = () => {
  /*
    WebSocket은 랜더링과 무관한 값
    state로하면 변경될 때마다 리랜더링
    값 유지 + 랜더링 영향 없음 = useRef
  */
  const chatBoxRef = useRef(null);
  const stompClient = useRef(null);
  const subscriptionRef = useRef(null);
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState("");
  const [roomName, setRoomName] = useState("");
  const senderId = useAuthStore((state) => state.memberId);
  const senderName = useAuthStore((state) => state.memberName);
  const senderThumb = useAuthStore((state) => state.memberThumb);
  const token = useAuthStore((state) => state.token);
  const isReady = useAuthStore((state) => state.isReady);
  const [marketNo, setMarketNo] = useState(null);
  const { roomId } = useParams();
  const navigate = useNavigate();

  useEffect(() => {
    if (!token) return;

    // 이전 채팅 get
    axios
      .get(`${import.meta.env.VITE_BACKSERVER}/chat/history/${roomId}`)
      .then((res) => {
        setMessages(res.data.messages); // 내용, 이름, 아이디
        setRoomName(res.data.roomName); // 채팅방 이름
        setMarketNo(res.data.marketNo); // 거래 게시글 번호 (상세 페이지 이동용)
      });

    connectWebsocket();

    // cleanup
    return () => {
      // 읽음 처리
      axios.post(`${import.meta.env.VITE_BACKSERVER}/chat/room/${roomId}/read`);

      subscriptionRef.current?.unsubscribe();
      stompClient.current?.deactivate();
    };
  }, [token]);

  useEffect(() => {
    if (chatBoxRef.current) {
      chatBoxRef.current.scrollTo({
        top: chatBoxRef.current.scrollHeight,
        behavior: "smooth",
      });
    }
  }, [messages]);

  const connectWebsocket = () => {
    if (stompClient.current?.active) return;

    const client = new Client({
      webSocketFactory: () =>
        // SockJS를 통해 WebSocket을 연결
        new SockJS(`${import.meta.env.VITE_BACKSERVER}/connect`),

      reconnectDelay: 5000,

      // 토큰을 가지고 요쳥
      connectHeaders: {
        Authorization: `${token}`,
      },

      // WebSocket 연결이 성공했을 때 자동으로 실행되는 함수
      onConnect: () => {
        if (subscriptionRef.current) {
          // 이미 구독 중인 게 있으면 먼저 해제하고 새로 구독(중복 방지)
          subscriptionRef.current.unsubscribe();
        }

        subscriptionRef.current = client.subscribe(
          `/topic/${roomId}`, // 구독할 경로

          (message) => {
            // 메시지 수신 시 실행할 함수
            let parseMessage;
            try {
              parseMessage = JSON.parse(message.body); // JSON이면 파싱
            } catch (e) {
              parseMessage = { message: message.body }; // 실패하면 그대로 저장
            }

            setMessages((prev) => [...prev, parseMessage]); // 메시지 목록에 추가
          },

          {
            Authorization: `${token}`, // subscribe 할 때도 토큰
          },
        );
      },
    });

    client.activate(); // 위 설정들을 기반으로 실제 연결 시작
    stompClient.current = client;
  };

  const sendMessage = (e) => {
    e.preventDefault(); // form submit 기본 동작 막기

    if (
      stompClient.current &&
      stompClient.current.connected &&
      newMessage.trim() !== ""
    ) {
      const obj = {
        senderId: senderId,
        senderName: senderName,
        senderThumb: senderThumb,
        message: newMessage,
      };

      stompClient.current.publish({
        destination: `/publish/${roomId}`,
        body: JSON.stringify(obj),
      });

      setNewMessage("");
    }
  };

  return (
    isReady && (
      <div className={styles.chat_card}>
        <h4
          className={styles.chat_room_title}
          onClick={() => {
            navigate(`/market/view/${marketNo}`);
          }}
        >
          {roomName}
        </h4>
        <div className={styles.chat_box} ref={chatBoxRef}>
          {messages.map((m, i) => (
            <div
              key={i}
              className={`${styles.chatting} ${m.senderId === senderId ? styles.sent : styles.received}`}
            >
              <div className={styles.chat_writer}>
                <div className={styles.chat_writer_thumb}>
                  <div
                    className={
                      m.senderThumb
                        ? styles.chat_thumb_exists
                        : styles.chat_thumb
                    }
                  >
                    {m.senderThumb ? (
                      <img
                        src={`${import.meta.env.VITE_BACKSERVER}/semi/${m.senderThumb}`}
                      />
                    ) : (
                      <span className="material-icons">account_circle</span>
                    )}
                  </div>
                </div>
                <div className={styles.chat_writer_name}>
                  <Nickname member={m} />
                </div>
              </div>
              <div className={styles.chat_message}>{m.message}</div>
            </div>
          ))}
        </div>
        <form className={styles.chat_send} onSubmit={sendMessage}>
          <textarea
            className={styles.input_zone}
            value={newMessage}
            onChange={(e) => setNewMessage(e.target.value)}
            placeholder="채팅을 입력하세요"
            onKeyDown={(e) => {
              if (e.key === "Enter" && !e.shiftKey) {
                e.preventDefault();
                sendMessage(e);
              }
            }}
          />
          <div className={styles.btn_zone}>
            <button type="submit">전송</button>
          </div>
        </form>
      </div>
    )
  );
};

export default StompChatPage;
