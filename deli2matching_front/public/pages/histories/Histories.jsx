import { useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../../utils/axios';
import styles from './Histories.module.css';

const Histories = () => {
  const navigate = useNavigate();
  const [histories, setHistories] = useState([]);
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef(null);

  useEffect(() => {
    axiosInstance.get('/histories')
      .then((res) => setHistories(res.data))
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

  return (
    <>
      <div className={`${styles.userInfo} ${menuOpen ? styles.open : ''}`} ref={menuRef}>
        <button className={styles.menuToggle} onClick={(e) => { e.stopPropagation(); setMenuOpen((o) => !o); }}>☰</button>
        <div className={styles.menuContent}>
          <button type="button" onClick={() => navigate('/matchings')}>매칭 목록</button>
          <button type="button" onClick={() => navigate('/users/reLocation')}>위치 변경</button>
          <button type="button" onClick={() => navigate('/matchings/my')}>나의 매칭</button>
          <button type="button" onClick={handleLogout}>로그아웃</button>
        </div>
      </div>

      <div className={styles.container}>
        <h1>성사된 매칭 목록</h1>
        <div className={styles.listContainer}>
          {histories.map((history) => (
            <button
              type="button"
              key={history.matchingId}
              className={styles.listMatching}
              onClick={() => navigate(`/histories/${history.matchingId}`)}
            >
              <div className={styles.matchingContent}>
                <div className={styles.matchingHeader}>{history.menu}</div>
                <div>제목 {history.title}</div>
                <div>날짜 {history.createdAt?.slice(0, 10)}</div>
              </div>
            </button>
          ))}
        </div>
      </div>
    </>
  );
};

export default Histories;
