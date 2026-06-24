import { useRef, useState } from "react";
import { useKakaoPostcode } from "@clroot/react-kakao-postcode";
import axiosInstance from "../../utils/axios";
import styles from "./MyInfo.module.css";

const MyInfo = () => {
  // isVerified: 비밀번호 확인 완료 여부 (false=확인화면, true=수정화면)
  const [isVerified, setIsVerified] = useState(false);
  const [password, setPassword] = useState("");

  const [form, setForm] = useState({
    nickname: "",
    loginId: "",
    email: "",
    userLocation: "",
  });

  // 일반 input 변경 핸들러
  const handleChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleVerify = (e) => {
    e.preventDefault();
    axiosInstance
      .post("/auth/verify-password", { password })
      .then(() => {
        // 확인 성공 시 내 정보 조회 후 폼에 채움
        return axiosInstance.get("/auth/myInfo");
      })
      .then((res) => {
        const d = res.data;
        setForm({
          nickname: d.nickname ?? "",
          loginId: d.loginId ?? "",
          email: d.email ?? "",
          userLocation: d.userLocation ?? "",
        });
        setIsVerified(true);
      })
      .catch((err) => {
        console.log(err);
        alert("비밀번호가 올바르지 않습니다.");
      });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    axiosInstance
      .put("/member/me", form)
      .then(() => alert("내 정보가 수정되었습니다."))
      .catch((err) => console.log(err));
  };

  // 이메일 인증 번호 발송
  const handleEmailVerify = () => {
    axiosInstance
      .post("/member/email-verify", { email: form.email })
      .catch((err) => console.log(err));
  };

  // 카카오 주소 검색
  const { open: openPostcode } = useKakaoPostcode({
    onComplete: (data) => {
      setForm((prev) => ({
        ...prev,
        zipCode: data.zonecode,
        address: data.roadAddress,
      }));
    },
  });

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>내 정보</h1>

      {/* ── Step 1: 비밀번호 확인 화면 ── */}
      {!isVerified && (
        <div className={styles.card}>
          <form onSubmit={handleVerify} className={styles.form}>
            <div className={styles.field_row}>
              <label className={styles.label}>비밀번호</label>
              <input
                type="password"
                className={styles.input}
                placeholder="비밀번호를 입력하세요"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            <button type="submit" className={styles.submit_btn}>
              내 정보 수정
            </button>
          </form>
        </div>
      )}

      {/* ── Step 2: 정보 수정 화면 ── */}
      {isVerified && (
        <div className={styles.card}>
          <form onSubmit={handleSubmit} className={styles.form}>
            {/* 이름 */}
            <div className={styles.field_row}>
              <label className={styles.label}>이름</label>
              <input
                type="text"
                name="name"
                className={styles.input}
                value={form.name}
                onChange={handleChange}
              />
            </div>

            {/* 아이디 */}
            <div className={styles.field_row}>
              <label className={styles.label}>아이디</label>
              <input
                type="text"
                name="memberId"
                className={styles.input}
                value={form.memberId}
                onChange={handleChange}
                readOnly /* 아이디는 변경 불가 */
              />
            </div>

            {/* 이메일 + 인증 번호 발송 */}
            <div className={styles.field_row}>
              <label className={styles.label}>이메일</label>
              <div className={styles.input_with_btn}>
                <input
                  type="email"
                  name="email"
                  className={styles.input}
                  value={form.email}
                  onChange={handleChange}
                />
                <button
                  type="button"
                  className={styles.inline_btn}
                  onClick={handleEmailVerify}
                >
                  인증 번호 발송
                </button>
              </div>
            </div>

            {/* 우편번호 + 주소 찾기 */}
            <div className={styles.field_row}>
              <label className={styles.label}>우편번호</label>
              <div className={styles.input_with_btn}>
                <input
                  type="text"
                  name="zipCode"
                  className={styles.input}
                  value={form.zipCode}
                  onChange={handleChange}
                  readOnly
                />
                <button
                  type="button"
                  className={styles.inline_btn}
                  onClick={openPostcode}
                >
                  주소 찾기
                </button>
              </div>
            </div>

            {/* 기본주소 */}
            <div className={styles.field_row}>
              <label className={styles.label}>기본주소</label>
              <input
                type="text"
                name="address"
                className={styles.input}
                value={form.address}
                onChange={handleChange}
                readOnly
                placeholder="주소 찾기를 이용해주세요"
              />
            </div>

            <button type="submit" className={styles.submit_btn}>
              내 정보 수정
            </button>
          </form>
        </div>
      )}
    </div>
  );
};

export default MyInfo;
