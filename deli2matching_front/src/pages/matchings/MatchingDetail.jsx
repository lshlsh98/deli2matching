import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import axiosInstance from '../../utils/axios';
import styles from './MatchingDetail.module.css';

const MatchingDetail = () => {
  const navigate = useNavigate();
  const { matchingId } = useParams();
  const [matching, setMatching] = useState(null);

  useEffect(() => {
    axiosInstance.get(`/matchings/${matchingId}`)
      .then((res) => setMatching(res.data))
      .catch((err) => console.error(err));
  }, [matchingId]);

  const handleJoin = async () => {
    try {
      await axiosInstance.post('/matchings/join', { matchingId: matching.matchingId });
      navigate('/matchings');
    } catch (err) {
      console.error(err);
    }
  };

  if (!matching) return null;

  const isFull = matching.headcount <= matching.count;

  return (
    <div className={styles.page}>
      <div className={styles.card} role="region" aria-label="매칭 상세 정보">
        <h1>매칭 상세 정보</h1>
        <div className={styles.matchingHead}>
          <div className={styles.title}>{matching.menu}</div>
          <div>제목: {matching.title}</div>
          <div>장소: {matching.detailLocation}</div>
          <div>방장: {matching.leader}</div>
          <div className={styles.metaRow}>
            <span>인원수: {matching.count} / {matching.headcount}</span>
          </div>
        </div>
        <div className={styles.matchingCreated}>
          생성일 {matching.createdAt?.slice(0, 10)}
        </div>
        <div className={styles.matchingBody}>{matching.description}</div>
        <div className={styles.actions}>
          {!isFull ? (
            <>
              <button type="button" className={styles.btnParticipate} onClick={handleJoin}>참가</button>
              <button type="button" className={styles.btnCancel} onClick={() => navigate('/matchings')}>취소</button>
            </>
          ) : (
            <button type="button" className={styles.btnCancel} onClick={() => navigate('/matchings')}>마감</button>
          )}
        </div>
      </div>
    </div>
  );
};

export default MatchingDetail;
