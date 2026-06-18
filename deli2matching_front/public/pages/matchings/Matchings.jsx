import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../../utils/axios';
import styles from './Matchings.module.css';

const Matchings = () => {
  const navigate = useNavigate();
  const [matchings, setMatchings] = useState([]);
  const [user, setUser] = useState({ nickname: '', userLocation: '' });
  const [keyword, setKeyword] = useState('');
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef(null);

  useEffect(() => {
    fetchMatchings('');
    const handleOutside = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target)) setMenuOpen(false);
    };
    document.addEventListener('click', handleOutside);
    return () => document.removeEventListener('click', handleOutside);
  }, []);

  const fetchMatchings = async (kw) => {
    try {
      const res = await axiosInstance.get('/matchings', { params: { keyword: kw } });
      setMatchings(res.data.matchings ?? res.data);
      if (res.data.user) setUser(res.data.user);
    } catch (err) {
      console.error(err);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    fetchMatchings(keyword);
  };

  const handleDetail = async (matchingId) => {
    try {
      await axiosInstance.post('/matchings/detail', { matchingId });
      navigate(`/matchings/${matchingId}`);
    } catch (err) {
      console.error(err);
    }
  };

  const handleLogout = async () => {
    await axiosInstance.post('/logout').catch(() => {});
    navigate('/');
  };

  return (
    <>
      <div className={`${styles.userInfo} ${menuOpen ? styles.open : ''}`} ref={menuRef}>
        <button className={styles.menuToggle} onClick={(e) => { e.stopPropagation(); setMenuOpen((o) => !o); }}>☰</button>
        <div className={styles.menuContent}>
          <button type="button" onClick={() => navigate('/histories')}>매칭 기록</button>
          <button type="button" onClick={() => navigate('/users/reLocation')}>위치 변경</button>
          <button type="button" onClick={() => navigate('/matchings/my')}>나의 매칭</button>
          <button type="button" onClick={handleLogout}>로그 아웃</button>
        </div>
      </div>

      <div className={styles.container}>
        <div className={styles.topSection}>
          <h3>
            <span>{user.nickname}</span> 님의 현재 위치는<br />
            <u>{user.userLocation}</u> 입니다.
          </h3>
          <form onSubmit={handleSearch}>
            <input
              type="text"
              className={styles.searchBox}
              name="keyword"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder="검색어를 입력하세요"
            />
            <button type="submit">검색</button>
          </form>
          <button className={styles.matchingAdd} onClick={() => navigate('/matchings/add')}>등록</button>
        </div>

        <div className={styles.listScrollArea}>
          {matchings.map((matching) => (
            <div className={styles.listContainer} key={matching.matchingId}>
              <button type="button" className={styles.listMatching} onClick={() => handleDetail(matching.matchingId)}>
                <div className={styles.matchingContent}>
                  <div className={styles.matchingHeader}>{matching.menu}</div>
                  <div>제목: {matching.title}</div>
                  <div>인원수: {matching.count} / {matching.headcount}</div>
                </div>
              </button>
            </div>
          ))}
        </div>
      </div>
    </>
  );
};

export default Matchings;
