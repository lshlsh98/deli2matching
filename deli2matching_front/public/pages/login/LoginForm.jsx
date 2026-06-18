import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../../utils/axios';
import useAuthStore from '../../utils/useAuthStore';
import styles from './LoginForm.module.css';

const LoginForm = () => {
  const navigate = useNavigate();
  const login = useAuthStore((state) => state.login);
  const [form, setForm] = useState({ loginId: '', password: '' });
  const [errors, setErrors] = useState({});

  useEffect(() => {
    axiosInstance.post('/logout').catch(() => {});
  }, []);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.id]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrors({});
    try {
      const res = await axiosInstance.post('/login', form);
      if (res.data.token) {
        login({ token: res.data.token });
        navigate('/matchings');
      }
    } catch (err) {
      if (err.response?.data) setErrors(err.response.data);
    }
  };

  return (
    <div className={styles.page}>
      <div className={styles.card} role="region" aria-label="로그인 폼">
        <h1>로그인 화면</h1>
        <form onSubmit={handleSubmit}>
          <div>
            <label htmlFor="loginId">아이디</label>
            <input type="text" id="loginId" value={form.loginId} onChange={handleChange} />
            {errors.loginId && <div className={styles.fieldError}>{errors.loginId}</div>}
          </div>
          <div>
            <label htmlFor="password">비밀번호</label>
            <input type="password" id="password" value={form.password} onChange={handleChange} />
            {errors.password && <div className={styles.fieldError}>{errors.password}</div>}
          </div>
          <div className={styles.btnRow}>
            <button className={styles.btnPrimary} type="submit">로그인</button>
            <button className={styles.btnSecondary} type="button" onClick={() => navigate('/')}>취소</button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default LoginForm;
