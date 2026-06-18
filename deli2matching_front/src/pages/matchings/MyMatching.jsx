import { useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../../utils/axios';
import styles from './MyMatching.module.css';

const MyMatching = () => {
  const navigate = useNavigate();
  const [data, setData] = useState({ myMatching: null, user: {}, isLeader: false, hasParticipant: '' });
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef(null);

  useEffect(() => {
    axiosInstance.get('/matchings/my')
      .then((res) => setData(res.data))
      .catch((err) => console.error(err));

    const handleOutside = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target)) setMenuOpen(false);
    };
    document.addEventListener('click', handleOutside);
    return () => document.removeEventListener('click', handleOutside);
  }, []);

  const handleLogout = async () => {
    await axiosInstance.post('/logout').catch(() => {});
    navigate('/');
  };

  const handleSuccessful = async () => {
    await axiosInstance.post('/matchings/successful', { matchingId: data.myMatching.matchingId });
    navigate('/matchings');
  };

  const handleDelete = async () => {
    await axiosInstance.post('/matchings/delete', { matchingId: data.myMatching.matchingId });
    navigate('/matchings');
  };

  const handleCancel = async () => {
    await axiosInstance.post('/matchings/cancel', { matchingId: data.myMatching.matchingId });
    navigate('/matchings');
  };

  const { myMatching, isLeader, hasParticipant } = data;

  return (
    <>
      <div className={`${styles.userInfo} ${menuOpen ? styles.open : ''}`} ref={menuRef}>
        <button className={styles.menuToggle} onClick={(e) => { e.stopPropagation(); setMenuOpen((o) => !o); }}>☰</button>
        <div className={styles.menuContent}>
          <button type="button" onClick={() => navigate('/histories')}>매칭 기록</button>
          <button type="button" onClick={() => navigate('/users/reLocation')}>위치 변경</button>
          <button type="button" onClick={handleLogout}>로그 아웃</button>
        </div>
      </div>

      {hasParticipant && (
        <div className={styles.hasParticipant} style={{ whiteSpace: 'pre-line' }}>{hasParticipant}</div>
      )}

      {!myMatching ? (
        <div className={styles.noMatching}>
          <div>현재 님의 매칭이 존재하지 않습니다.<br />매칭 찾기를 통해 매칭을 만들어 주세요.</div>
          <button className={styles.noMatchingBtn} type="button" onClick={() => navigate('/users/location')}>매칭 찾기</button>
        </div>
      ) : (
        <div className={styles.listMatching}>
          <div className={styles.page}>
            <div className={styles.card} role="region" aria-label="매칭 상세 정보">
              <h1>매칭 상세 정보</h1>
              <div className={styles.matchingHead}>
                <div className={styles.title}>{myMatching.menu}</div>
                <div>제목 {myMatching.title}</div>
                <div>장소 {myMatching.detailLocation}</div>
                <div>방장 {myMatching.leader}</div>
                <div className={styles.metaRow}>
                  <span>인원수 {myMatching.count} / {myMatching.headcount}</span>
                </div>
              </div>
              <div className={styles.matchingCreated}>생성일 {myMatching.createdAt?.slice(0, 10)}</div>
              <div className={styles.matchingBody}>{myMatching.description}</div>
            </div>

            <div className={styles.actionsContainer}>
              <div className={styles.successActions}>
                {isLeader && (
                  <button type="button" className={styles.btnSuccess} onClick={handleSuccessful}>매칭 성사</button>
                )}
              </div>
              <div className={styles.deleteCancelActions}>
                {isLeader ? (
                  <button type="button" className={styles.btnCancel} onClick={handleDelete}>매칭 삭제</button>
                ) : (
                  <button type="button" className={styles.btnCancel} onClick={handleCancel}>매칭 취소</button>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default MyMatching;
