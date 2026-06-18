import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../../utils/axios';
import styles from './UserAddForm.module.css';

const UserAddForm = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState({ loginId: '', password: '', phoneNumber: '', nickname: '' });
  const [errors, setErrors] = useState({});

  const handleChange = (e) => {
    setForm({ ...form, [e.target.id]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrors({});
    try {
      await axiosInstance.post('/users/add', form);
      navigate('/');
    } catch (err) {
      if (err.response?.data) setErrors(err.response.data);
    }
  };

  return (
    <form onSubmit={handleSubmit} className={styles.form}>
      {errors.global && (
        <p className={styles.fieldError}>{errors.global}</p>
      )}
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
      <div>
        <label htmlFor="phoneNumber">전화번호 010-</label>
        <input type="text" id="phoneNumber" value={form.phoneNumber} onChange={handleChange} placeholder="- 제외하고 입력하세요." />
        {errors.phoneNumber && <div className={styles.fieldError}>{errors.phoneNumber}</div>}
      </div>
      <div>
        <label htmlFor="nickname">닉네임</label>
        <input type="text" id="nickname" value={form.nickname} onChange={handleChange} />
        {errors.nickname && <div className={styles.fieldError}>{errors.nickname}</div>}
      </div>
      <div className={styles.buttonRow}>
        <button type="submit">가입</button>
        <button type="button" onClick={() => navigate('/')}>취소</button>
      </div>
    </form>
  );
};

export default UserAddForm;
