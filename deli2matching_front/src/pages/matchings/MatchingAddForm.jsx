import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../../utils/axios';
import styles from './MatchingAddForm.module.css';

const MatchingAddForm = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState({ menu: '', detailLocation: '', headcount: '', title: '', description: '' });
  const [errors, setErrors] = useState({});

  const handleChange = (e) => {
    setForm({ ...form, [e.target.id]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrors({});
    try {
      await axiosInstance.post('/matchings', form);
      navigate('/matchings');
    } catch (err) {
      if (err.response?.data) setErrors(err.response.data);
    }
  };

  return (
    <form onSubmit={handleSubmit} className={styles.form}>
      <h1>메뉴 등록 화면</h1>
      <div>
        <label htmlFor="menu">메뉴</label>
        <input type="text" id="menu" value={form.menu} onChange={handleChange} />
        {errors.menu && <div className={styles.fieldError}>{errors.menu}</div>}
      </div>
      <div>
        <label htmlFor="detailLocation">세부 장소</label>
        <input type="text" id="detailLocation" value={form.detailLocation} onChange={handleChange} />
        {errors.detailLocation && <div className={styles.fieldError}>{errors.detailLocation}</div>}
      </div>
      <div>
        <label htmlFor="headcount">인원수</label>
        <input type="text" id="headcount" value={form.headcount} onChange={handleChange} placeholder="2명 이상이어야 합니다" />
        {errors.headcount && <div className={styles.fieldError}>{errors.headcount}</div>}
      </div>
      <div>
        <label htmlFor="title">제목</label>
        <input type="text" id="title" value={form.title} onChange={handleChange} />
        {errors.title && <div className={styles.fieldError}>{errors.title}</div>}
      </div>
      <div>
        <label htmlFor="description">설명</label>
        <textarea id="description" value={form.description} onChange={handleChange} maxLength={60} placeholder="60자 이하로 입력해주세요" />
        {errors.description && <div className={styles.fieldError}>{errors.description}</div>}
      </div>
      <div className={styles.buttonRow}>
        <button type="submit">등록</button>
        <button type="button" onClick={() => navigate('/matchings')}>취소</button>
      </div>
    </form>
  );
};

export default MatchingAddForm;
