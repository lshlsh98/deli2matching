import { useEffect, useRef, useState } from 'react';
import { useParams } from 'react-router-dom';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import axiosInstance from '../../utils/axios';
import styles from './Chatting.module.css';

const API_BASE_URL = '';

const Chatting = () => {
  const { roomId, leader } = useParams();
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [nickname, setNickname] = useState('');
  const chatBoxRef = useRef(null);
  const stompClientRef = useRef(null);

  useEffect(() => {
    axiosInstance.get('/users/me')
      .then((res) => setNickname(res.data.nickname))
      .catch(() => {});
  }, []);

  useEffect(() => {
    if (!roomId) return;

    axiosInstance.get(`/chat/history/${encodeURIComponent(roomId)}`)
      .then((res) => { if (Array.isArray(res.data)) setMessages(res.data); })
      .catch(() => {});

    const sockJS = new SockJS(`${API_BASE_URL}/connect`);
    const client = new Client({
      webSocketFactory: () => sockJS,
      onConnect: () => {
        client.subscribe(`/topic/${roomId}`, (message) => {
          const parsed = JSON.parse(message.body);
          setMessages((prev) => [...prev, parsed]);
        });
      },
    });
    client.activate();
    stompClientRef.current = client;

    return () => { client.deactivate(); };
  }, [roomId]);

  useEffect(() => {
    if (chatBoxRef.current) {
      chatBoxRef.current.scrollTop = chatBoxRef.current.scrollHeight;
    }
  }, [messages]);

  const sendMessage = () => {
    const text = input.trim();
    if (!text || !stompClientRef.current?.connected) return;
    stompClientRef.current.publish({
      destination: `/publish/${roomId}`,
      body: JSON.stringify({ senderNickname: nickname, message: text }),
    });
    setInput('');
  };

  return (
    <div className={styles.chatContainer}>
      <div className={styles.chatTitle}>채팅</div>
      <div id="chatBox" className={styles.chatBox} ref={chatBoxRef}>
        {messages.map((msg, i) => (
          <div
            key={i}
            className={`${styles.chatMessage} ${msg.senderNickname === nickname ? styles.sent : styles.received}`}
          >
            <strong>
              {msg.senderNickname === leader ? `👑 ${msg.senderNickname}` : msg.senderNickname}:{' '}
            </strong>
            <span>{msg.message}</span>
          </div>
        ))}
      </div>
      <div className={styles.inputRow}>
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); sendMessage(); } }}
          placeholder="메시지 입력"
        />
        <button type="button" onClick={sendMessage}>전송</button>
      </div>
    </div>
  );
};

export default Chatting;
