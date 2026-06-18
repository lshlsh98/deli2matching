import { useNavigate } from 'react-router-dom';
import styles from './Home.module.css';

const Home = () => {
  const navigate = useNavigate();

  return (
    <>
      <div className={styles.content}>
        <img src="/images/order_together.png" alt="함께 주문하기 이미지" />
      </div>
      <div className={styles.footer}>
        <button type="button" onClick={() => navigate('/login')}>로그인</button>
        <button type="button" onClick={() => navigate('/signup')}>회원 가입</button>
      </div>
    </>
  );
};

export default Home;
