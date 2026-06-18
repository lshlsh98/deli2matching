import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../../utils/axios';
import styles from './LoginHome.module.css';

const LoginHome = () => {
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        setMenuOpen(false);
      }
    };
    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  }, []);

  const handleLogout = async () => {
    await axiosInstance.post('/logout').catch(() => {});
    navigate('/');
  };

  const handleMatchingFind = () => {
    if (window.Android?.onLoginSuccess) {
      window.Android.onLoginSuccess();
    }
    navigate('/users/location');
  };

  return (
    <>
      <div className={`${styles.userInfo} ${menuOpen ? styles.open : ''}`} ref={menuRef}>
        <button
          className={styles.menuToggle}
          onClick={(e) => { e.stopPropagation(); setMenuOpen((o) => !o); }}
        >
          ☰
        </button>
        <div className={styles.menuContent}>
          <button type="button" onClick={handleLogout}>로그아웃</button>
        </div>
      </div>

      <div className={styles.notice}>
        <img src="/images/user_location.png" alt="함께 주문하기 이미지" className={styles.noticeImage} />
      </div>

      <div className={styles.bottomButton}>
        <button type="button" onClick={handleMatchingFind}>매칭 찾기</button>
      </div>
    </>
  );
};

export default LoginHome;
