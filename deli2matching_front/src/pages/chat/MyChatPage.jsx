import { useEffect, useState } from "react";
import styles from "./MyChatPage.module.css";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import useAuthStore from "../utils/useAuthStore";
import Nickname from "../commons/Nickname";

const MyChatPage = () => {
  const [list, setList] = useState([]);
  const navigate = useNavigate();
  const myName = useAuthStore((state) => state.memberName);

  useEffect(() => {
    // 내 채팅방 get
    axios
      .get(`${import.meta.env.VITE_BACKSERVER}/chat/my/rooms`)
      .then((res) => {
        setList(res.data);
      })
      .catch((err) => {
        console.log(err);
      });
  }, []);

  const enterChatRoom = (roomId) => {
    navigate(`/chatpage/${roomId}`);
  };

  return (
    <div className={styles.content}>
      {list.length === 0 ? (
        <div className={styles.noroom}>
          <h3 className="page-title">
            "현재 참여하고 있는 채팅방이 없습니다."
          </h3>
        </div>
      ) : (
        <div className={styles.chat_list_wrap}>
          <ul className={`${styles.chat_item} ${styles.title_ul}`}>
            <li className={styles.chat_room_name}>채팅방 이름</li>
            <li className={styles.chat_other}>상대방</li>
            <li className={styles.chat_unread_count}>읽지 않은 메시지</li>
            <li className={styles.chat_btn}>입장</li>
          </ul>
          {list.map((chat) => (
            <ul key={chat.roomId} className={styles.chat_item}>
              <li
                className={styles.chat_room_name}
                onClick={() => {
                  navigate(`/market/view/${chat.marketNo}`);
                }}
              >
                {chat.roomName}
              </li>
              <li className={styles.chat_other}>
                {myName === chat.myName ? (
                  <Nickname
                    member={{
                      memberName: chat.otherName,
                      memberId: chat.otherId,
                      hexCode: chat.otherHexCode,
                    }}
                  />
                ) : (
                  <Nickname
                    member={{
                      memberName: chat.myName,
                      memberId: chat.myId,
                      hexCode: chat.myHexCode,
                    }}
                  />
                )}
              </li>
              <li className={styles.chat_unread_count}>{chat.unReadCount}</li>
              <li className={styles.chat_btn}>
                <button
                  onClick={() => {
                    enterChatRoom(chat.roomId);
                  }}
                >
                  입장
                </button>
              </li>
            </ul>
          ))}
        </div>
      )}
    </div>
  );
};

export default MyChatPage;
