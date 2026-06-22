import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useKakaoPostcode } from "@clroot/react-kakao-postcode";
import axiosInstance from "../../utils/axios";
import styles from "./DeliveryRegist.module.css";

// 모집 마감 시간 선택지: label(표시용) + minutes(실제 계산용)
const DEADLINE_OPTIONS = [
  { label: "30분 뒤", minutes: 30 },
  { label: "1시간 뒤", minutes: 60 },
  { label: "1시간 30분 뒤", minutes: 90 },
  { label: "2시간 뒤", minutes: 120 },
  { label: "3시간 뒤", minutes: 180 },
];

// DeliveryRegist: 배달 모집 등록 페이지
const DeliveryRegist = () => {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    restaurantName: "",
    targetMembers: 2,
    deadlineMinutes: 30,
    pickupLocation: "",
    detailLocation: "",
    memo: "",
  });

  const handleChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  // 최소 2명, 최대 10명으로 제한
  const handleMemberStep = (delta) => {
    setForm((prev) => ({
      ...prev,
      targetMembers: Math.max(2, Math.min(10, prev.targetMembers + delta)),
    }));
  };

  const { open: openPostcode } = useKakaoPostcode({
    onComplete: (data) => {
      setForm((prev) => ({ ...prev, pickupLocation: data.roadAddress }));
    },
  });

  const handleSubmit = (e) => {
    e.preventDefault();

    // deadline_at: 현재 시각 + 선택한 분(deadlineMinutes)
    const deadlineAt = new Date(
      Date.now() + Number(form.deadlineMinutes) * 60 * 1000,
    ).toISOString();

    axiosInstance
      .post("/delivery", {
        restaurantName: form.restaurantName,
        targetMembers: form.targetMembers,
        pickupLocation: form.pickupLocation,
        detailLocation: form.detailLocation,
        memo: form.memo || null, // 빈 문자열은 NULL로 전송
        deadlineAt,
      })
      .then(() => {
        navigate("/"); // 등록 성공 시 홈으로 이동
      })
      .catch((err) => {
        console.log(err);
      });
  };

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>배달 모집 등록</h1>

      <div className={styles.card}>
        <form onSubmit={handleSubmit} className={styles.form}>
          <div className={styles.field_group}>
            <label className={styles.label}>가게 이름</label>
            <input
              type="text"
              name="restaurantName"
              className={styles.input}
              value={form.restaurantName}
              onChange={handleChange}
              required
            />
          </div>

          <div className={styles.row}>
            <div className={styles.field_group}>
              <label className={styles.label}>목표 모집 인원</label>
              <div className={styles.stepper}>
                <button
                  type="button"
                  className={styles.stepper_btn}
                  onClick={() => handleMemberStep(-1)}
                >
                  &#x2296;
                </button>
                <span className={styles.stepper_value}>
                  {form.targetMembers}
                </span>
                <button
                  type="button"
                  className={styles.stepper_btn}
                  onClick={() => handleMemberStep(1)}
                >
                  &#x2295;
                </button>
              </div>
            </div>

            <div className={styles.field_group}>
              <label className={styles.label}>모집 마감 시간</label>
              <select
                name="deadlineMinutes"
                className={styles.select}
                value={form.deadlineMinutes}
                onChange={handleChange}
              >
                {DEADLINE_OPTIONS.map((opt) => (
                  <option key={opt.minutes} value={opt.minutes}>
                    {opt.label}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className={styles.field_group}>
            <label className={styles.label}>수령 희망 장소</label>
            <div className={styles.location_row}>
              <input
                type="text"
                name="pickupLocation"
                className={styles.input}
                value={form.pickupLocation}
                onChange={handleChange}
                required
                readOnly
              />
              <button
                type="button"
                className={styles.map_btn}
                onClick={openPostcode}
              >
                주소 찾기
              </button>
            </div>
          </div>

          <div className={styles.field_group}>
            <label className={styles.label}>상세 장소</label>
            <input
              type="text"
              name="detailLocation"
              className={styles.input}
              placeholder="예: 1생활관 A동 현관 앞"
              value={form.detailLocation}
              onChange={handleChange}
              required
            />
          </div>

          <div className={styles.field_group}>
            <label className={styles.label}>메모 (선택사항)</label>
            <textarea
              name="memo"
              className={styles.textarea}
              placeholder="예: 메뉴는 채팅방에서 같이 골라요! 주문 후 픽업 장소에서 만나요."
              value={form.memo}
              onChange={handleChange}
              rows={4}
            />
          </div>

          {/* ── 등록하기 버튼 ── */}
          <button type="submit" className={styles.submit_btn}>
            등록하기
          </button>
        </form>
      </div>
    </div>
  );
};

export default DeliveryRegist;
