import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useKakaoPostcode } from "@clroot/react-kakao-postcode";
import axiosInstance from "../../utils/axios";
import styles from "./DeliveryRegist.module.css";
import BasicSelect from "../ui/BasicSelect";
import Swal from "sweetalert2";

const DEADLINE_OPTIONS = [
  [30, "30분 뒤"],
  [60, "1시간 뒤"],
  [90, "1시간 30분 뒤"],
  [120, "2시간 뒤"],
  [180, "3시간 뒤"],
];

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

    // toISOString()은 UTC 기준이라 KST와 9시간 차이남
    // → KST 오프셋을 더한 뒤 Z를 제거해서 "시간대 없는 KST 문자열"로 전송
    const KST_OFFSET = 9 * 60 * 60 * 1000;
    const deadlineAt = new Date(
      Date.now() + KST_OFFSET + Number(form.deadlineMinutes) * 60 * 1000,
    )
      .toISOString()
      .replace("Z", ""); // "2026-06-22T21:03:38.000" 형태로 전송

    axiosInstance
      .post("/delivery", {
        restaurantName: form.restaurantName,
        targetMembers: form.targetMembers,
        pickupLocation: form.pickupLocation,
        detailLocation: form.detailLocation,
        memo: form.memo || null, // 빈 문자열은 NULL로 전송
        deadlineAt,
      })
      .then((res) => {
        Swal.fire({
          icon: "success",
          title: "배달 모집 완료!",
          text: "배달 모집이 성공적으로 등록되었습니다.",
          confirmButtonText: "확인",
        }).then((result) => {
          if (result.isConfirmed) {
            navigate(`/view/${res.data}`);
          }
        });
      })
      .catch((err) => {
        if (err.response?.status === 409) {
          alert("현재 진행 중인 배달 모집이 있습니다.");
          return;
        }
      });
  };

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <form onSubmit={handleSubmit} className={styles.form}>
          <div className={styles.field_group}>
            <label className={styles.label}>가게 이름</label>
            <input
              type="text"
              name="restaurantName"
              className={styles.input}
              placeholder="예: BBQ 치킨 강남점"
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
              <BasicSelect
                state={form.deadlineMinutes}
                setState={(value) =>
                  setForm((prev) => ({ ...prev, deadlineMinutes: value }))
                }
                list={DEADLINE_OPTIONS}
              />
            </div>
          </div>

          <div className={styles.field_group}>
            <label className={styles.label}>장소</label>
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

          <button type="submit" className={styles.submit_btn}>
            등록하기
          </button>
        </form>
      </div>
    </div>
  );
};

export default DeliveryRegist;
