import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import axiosInstance from '../../utils/axios';
import styles from './HistoryDetail.module.css';

const HistoryDetail = () => {
  const navigate = useNavigate();
  const { matchingId } = useParams();
  const [history, setHistory] = useState(null);

  useEffect(() => {
    axiosInstance.get('/histories/detail', { params: { matchingId } })
      .then((res) => setHistory(res.data))
      .catch((err) => console.error(err));
  }, [matchingId]);

  if (!history) return null;

  return (
    <div className={styles.page}>
      <div className={styles.card} role="region" aria-label="매칭 상세 정보">
        <h1>성사 매칭 상세 정보</h1>
        <div className={styles.matchingHead}>
          <div className={styles.title}>{history.menu}</div>
          <div>제목: {history.title}</div>
          <div>장소: {history.detailLocation}</div>
          <div>방장: {history.leader}</div>
        </div>
        <div className={styles.matchingCreated}>날짜 {history.createdAt?.slice(0, 10)}</div>
        <div className={styles.matchingBody}>{history.description}</div>
        <button
          type="button"
          className={styles.chatButton}
          onClick={() => navigate(`/chatting/${history.matchingId}/${history.leader}`)}
        >
          채팅
        </button>
      </div>
    </div>
  );
};

export default HistoryDetail;
