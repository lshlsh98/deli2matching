import { useRef, useState } from "react";
import { useKakaoPostcode } from "@clroot/react-kakao-postcode";
import axiosInstance from "../../utils/axios";
import styles from "./MyInfo.module.css";
import Swal from "sweetalert2";

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

  // ── Step 1: 비밀번호 확인 ──
  const handleVerify = (e) => {
    e.preventDefault();
    console.log(password);
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

  // ── Step 2: 정보 수정 제출 ──
  const handleSubmit = (e) => {
    e.preventDefault();

    // 닉네임 중복 체크 완료 여부 확인
    if (checkName === 2) {
      Swal.fire({
        icon: "warning",
        title: "확인 필요",
        text: "닉네임 중복을 확인해주세요.",
      });
      return;
    }

    // 이메일을 변경했다면 인증 완료 여부 확인
    if (mailAuth !== 0 && mailAuth !== 3) {
      Swal.fire({
        icon: "warning",
        title: "인증 필요",
        text: "이메일 인증을 완료해주세요.",
      });
      return;
    }

    axiosInstance
      .put("/auth/myInfo", form)
      .then(() =>
        Swal.fire({
          icon: "success",
          title: "수정 완료",
          text: "내 정보가 수정되었습니다.",
        }),
      )
      .catch((err) => console.log(err));
  };

  // ── 닉네임 중복 체크 (SignUp의 nameDupCheck와 동일한 패턴) ──
  // 0: 미확인 / 1: 사용 가능 / 2: 중복
  const [checkName, setCheckName] = useState(0);

  const nameDupCheck = () => {
    if (form.nickname === "") {
      setCheckName(0);
      return;
    }
    axiosInstance
      .get(`/auth/nameExists?memberName=${form.nickname}`)
      .then((res) => {
        if (res.data) {
          setCheckName(2); // 중복
        } else {
          setCheckName(1); // 사용 가능
        }
      })
      .catch((err) => console.log(err));
  };

  // ── 이메일 인증 (SignUp의 sendMail / 인증하기와 동일한 패턴) ──
  // 0: 대기 / 1: 발송 중 / 2: 코드 입력 대기 / 3: 인증 성공
  const [mailAuth, setMailAuth] = useState(0);
  const [mailAuthCode, setMailAuthCode] = useState(null); // 서버에서 받은 인증코드
  const [mailAuthInput, setMailAuthInput] = useState(""); // 사용자 입력 인증코드
  const [time, setTime] = useState(300); // 타이머 300초
  const timerRef = useRef(null); // 타이머 ref

  // 분:초 포맷
  const showTime = () => {
    const min = Math.floor(time / 60);
    const sec = String(time % 60).padStart(2, "0");
    return `${min}:${sec}`;
  };

  // 인증 코드 발송
  const sendMail = () => {
    if (form.email === "") {
      Swal.fire({
        icon: "warning",
        title: "이메일 입력",
        text: "이메일을 먼저 입력해주세요.",
      });
      return;
    }

    // 타이머 초기화 후 재시작
    setTime(300);
    setMailAuthCode(null);
    if (timerRef.current) window.clearInterval(timerRef.current);

    setMailAuth(1);
    Swal.fire({ title: "메일 발송 중...", didOpen: () => Swal.showLoading() });

    axiosInstance
      .post("/auth/email-verification", { memberEmail: form.email })
      .then((res) => {
        Swal.fire({
          icon: "success",
          title: "발송 완료",
          text: "이메일로 인증번호가 발송되었습니다.",
        });
        setMailAuthCode(res.data);
        setMailAuth(2);

        // 5분 카운트다운 타이머
        timerRef.current = window.setInterval(() => {
          setTime((prev) => {
            if (prev <= 1) {
              window.clearInterval(timerRef.current);
              Swal.fire({
                icon: "warning",
                title: "시간 초과",
                text: "인증 시간이 만료되었습니다. 다시 시도해주세요.",
              });
              setMailAuthCode(null);
              setMailAuth(0);
              return 0;
            }
            return prev - 1;
          });
        }, 1000);
      })
      .catch((err) => {
        console.log(err);
        Swal.fire({
          icon: "error",
          title: "발송 실패",
          text: "이메일 발송에 실패했습니다. 입력하신 이메일을 확인해주세요!",
        });
        setMailAuth(0);
      });
  };

  // 인증코드 확인
  const verifyMailCode = () => {
    if (mailAuthCode === mailAuthInput && mailAuthInput !== "") {
      setMailAuth(3);
      window.clearInterval(timerRef.current);
      Swal.fire({
        icon: "success",
        title: "인증 성공",
        text: "이메일 인증이 완료되었습니다.",
      });
    } else {
      Swal.fire({
        icon: "error",
        title: "인증 실패",
        text: "인증코드가 올바르지 않습니다.",
      });
    }
  };

  // 카카오 주소 검색
  const { open: openPostcode } = useKakaoPostcode({
    onComplete: (data) => {
      setForm((prev) => ({ ...prev, userLocation: data.roadAddress }));
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
            {/* 닉네임 + 중복 체크 (onBlur로 자동 확인) */}
            <div className={styles.field_row}>
              <label className={styles.label}>닉네임</label>
              <input
                type="text"
                name="nickname"
                className={styles.input}
                value={form.nickname}
                onChange={handleChange}
                onBlur={nameDupCheck} /* 포커스 벗어날 때 중복 체크 */
              />
            </div>
            {/* 닉네임 중복 체크 결과 메시지 */}
            {checkName > 0 && (
              <p
                className={`${styles.validation_msg} ${checkName === 1 ? styles.valid : styles.invalid}`}
              >
                {checkName === 1 && "사용 가능한 닉네임입니다."}
                {checkName === 2 && "이미 사용 중인 닉네임입니다."}
              </p>
            )}

            {/* 아이디 (변경 불가) */}
            <div className={styles.field_row}>
              <label className={styles.label}>아이디</label>
              <input
                type="text"
                name="loginId"
                className={styles.input}
                value={form.loginId}
                readOnly /* 아이디는 변경 불가 */
              />
            </div>

            {/* 이메일 + 인증 코드 전송 */}
            <div className={styles.field_row}>
              <label className={styles.label}>이메일</label>
              <div className={styles.input_with_btn}>
                <input
                  type="email"
                  name="email"
                  className={styles.input}
                  value={form.email}
                  onChange={handleChange}
                  readOnly={
                    mailAuth === 1 || mailAuth === 3
                  } /* 발송 중 또는 인증 완료 시 잠금 */
                />
                <button
                  type="button"
                  className={styles.inline_btn}
                  onClick={sendMail}
                  disabled={mailAuth === 1 || mailAuth === 3}
                >
                  {mailAuth >= 2 ? "재전송" : "인증 코드 전송"}
                </button>
              </div>
            </div>

            {/* 인증 코드 입력 (코드 발송 후에만 표시) */}
            {mailAuth > 1 && (
              <div className={styles.field_row}>
                <label className={styles.label}>인증 코드</label>
                <div className={styles.input_with_btn}>
                  <div className={styles.timer_wrap}>
                    <input
                      type="text"
                      className={styles.input}
                      placeholder="인증코드를 입력하세요"
                      value={mailAuthInput}
                      onChange={(e) => setMailAuthInput(e.target.value)}
                      disabled={mailAuth === 3} /* 인증 완료 시 잠금 */
                    />
                    {/* 남은 시간 표시 (인증 완료 전까지만) */}
                    {mailAuth !== 3 && (
                      <span className={styles.timer_text}>{showTime()}</span>
                    )}
                  </div>
                  <button
                    type="button"
                    className={styles.inline_btn}
                    onClick={verifyMailCode}
                    disabled={mailAuth === 3}
                  >
                    인증하기
                  </button>
                </div>
              </div>
            )}
            {/* 이메일 인증 결과 메시지 */}
            {mailAuth === 3 && (
              <p className={`${styles.validation_msg} ${styles.valid}`}>
                인증되었습니다.
              </p>
            )}

            {/* 주소 + 주소 찾기 */}
            <div className={styles.field_row}>
              <label className={styles.label}>주소</label>
              <div className={styles.input_with_btn}>
                <input
                  type="text"
                  name="userLocation"
                  className={styles.input}
                  value={form.userLocation}
                  placeholder="도로명주소"
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
