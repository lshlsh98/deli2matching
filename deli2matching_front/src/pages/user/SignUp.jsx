import { Link, useNavigate } from "react-router-dom";
import axiosInstance from "../../utils/axios";
import { useRef, useState } from "react";
import { useKakaoPostcode } from "@clroot/react-kakao-postcode";
import styles from "./SignUp.module.css";
import Swal from "sweetalert2";

// SignUp 컴포넌트: 회원가입 페이지 화면
const SignUp = () => {
  // const navigate = useNavigate();

  // const [member, setMember] = useState({
  //   loginId: "",
  //   password: "",
  //   username: "",
  // });

  // const inputMember = (e) => {
  //   const newMember = { ...member, [e.target.name]: e.target.value };
  //   setMember(newMember);
  // };

  // const joinMember = () => {
  //   // 서버에 회원가입 요청을 보냄
  //   // POST /auth/signup → 서버의 회원가입 처리 주소
  //   axiosInstance
  //     .post("/auth/signup", member)
  //     .then(() => {
  //       navigate("/login");
  //     })
  //     .catch((err) => {
  //       console.log(err);
  //     });
  // };

  // return (
  //   <section>
  //     <h3>회원가입</h3>
  //     <form
  //       onSubmit={(e) => {
  //         e.preventDefault();
  //         joinMember();
  //       }}
  //       autoComplete="off"
  //     >
  //       <div>
  //         <label htmlFor="memberId">아이디</label>
  //         <input
  //           type="text"
  //           name="loginId"
  //           id="loginId"
  //           value={member.loginId}
  //           onChange={inputMember}
  //         />
  //       </div>

  //       <div>
  //         <label htmlFor="password">비밀번호</label>
  //         <input
  //           type="password"
  //           name="password"
  //           id="password"
  //           value={member.password}
  //           onChange={inputMember}
  //         />
  //       </div>
  //       <div>
  //         <label htmlFor="username">이름</label>
  //         <input
  //           type="text"
  //           name="username"
  //           id="username"
  //           value={member.username}
  //           onChange={inputMember}
  //         />
  //       </div>
  //       <div>
  //         <button type="submit">회원가입</button>
  //       </div>
  //     </form>
  //   </section>
  // );

  const navigate = useNavigate();
  const detailRef = useRef(null); //카카오 맵으로 주소를 고르면 상세주소로 focus가 가게끔 하기위해 상세주소input에 이름표 달아두기용

  const [member, setMember] = useState({
    memberId: "",
    memberPw: "",
    memberName: "",
    memberEmail: "",
    memberAddr: "",
  });

  const [memberPwRe, setMemberPwRe] = useState(""); // 비밀번호 확인 value용 state

  const [pwVisible, setPwVisible] = useState(false); // 비번 숨김 / 보임용
  const [pwReVisible, setPwReVisible] = useState(false); // 비번 확인의 숨김 / 보임용

  const inputMember = (e) => {
    setMember({ ...member, [e.target.name]: e.target.value });
  };

  const [checkId, setCheckId] = useState(0); // 중복 확인용 state
  const [checkName, setCheckName] = useState(0); // 중복 확인용 state

  // 아이디 중복 체크 및 유효성 검사
  const idDupCheck = () => {
    // 회원 아이디가 비어있다면
    if (member.memberId === "") {
      // 아이디 체크해서 에러 문구띄우는 state인 checkId 0으로 초기화
      setCheckId(0);
      return;
    }

    // 아이디 정규식: 영문 1개 이상 필수, 숫자/특수문자 선택, 6자 이상 (한글 불가)
    const idRegex =
      /^(?=.*[a-zA-Z])[a-zA-Z0-9!@#$%^&*()_+~`\-={}\[\]:;"'<>,.?\/\\|]{6,}$/;
    // .text => 정규식 규칙이 맞는지 검사해서 true / false 반환
    if (!idRegex.test(member.memberId)) {
      // 정규식이 false이니 형식 오류 반환
      setCheckId(3); // 3: 형식 오류
      return;
    }

    axiosInstance
      .get(`/auth/idExists/?memberId=${member.memberId}`)
      .then((res) => {
        if (res.data) {
          setCheckId(2); // 2: 중복 (이미 있음)
        } else {
          setCheckId(1); // 1: 사용 가능
        }
      })
      .catch((err) => {
        console.log(err);
      });
  };

  // 닉네임 중복 체크 및 유효성 검사
  const nameDupCheck = () => {
    // 회원 닉네임이 비어있다면
    if (member.memberName === "") {
      // 이름 체크해서 에러 문구띄우는 state인 checkId 0으로 초기화
      setCheckName(0);
      return;
    }

    axiosInstance
      .get(`/auth/nameExists?memberName=${member.memberName}`)
      .then((res) => {
        if (res.data) {
          setCheckName(2); // 2: 중복 (이미 있음)
        } else {
          setCheckName(1); // 1: 사용 가능
        }
      })
      .catch((err) => {
        console.log(err);
      });
  };

  const [checkPw, setCheckPw] = useState(0); // 비밀번호 확인 맞는지 틀린지 보는용도 state

  // 비밀번호 유효성 검사 및 일치 여부
  const pwDupCheck = () => {
    // 회원 비번이 공백이면
    if (member.memberPw === "") {
      // 비번 체크해서 에러 문구띄우는 state인 checkPw 0으로 초기화
      setCheckPw(0);
      return;
    }

    // 비밀번호 정규식: 영문 1개 이상, 특수문자 1개 이상 필수, 숫자 선택, 8자 이상
    const pwRegex =
      /^(?=.*[a-zA-Z])(?=.*[!@#$%^&*()_+~`\-={}\[\]:;"'<>,.?\/\\|])[a-zA-Z\d!@#$%^&*()_+~`\-={}\[\]:;"'<>,.?\/\\|]{8,}$/;

    if (!pwRegex.test(member.memberPw)) {
      // 정규식이 false이니 형식 오류 반환
      setCheckPw(3); // 3: 형식 오류
      return;
    }

    // 형식이 맞으면, 이제 비밀번호 확인(memberPwRe)과 일치하는지 검사
    if (memberPwRe === "") {
      setCheckPw(0); // 아직 확인 칸을 안 쳤으면 아무 메시지도 안 띄움
    } else if (member.memberPw === memberPwRe) {
      setCheckPw(1); // 1: 일치
    } else {
      setCheckPw(2); // 2: 불일치
    }
  };

  const { open } = useKakaoPostcode({
    onComplete: (data) => {
      setMember({
        ...member,
        memberAddr: data.roadAddress, // roadAddress : 도로명 주소
      });
      detailRef.current.focus();
    },
  });

  const [mailAuth, setMailAuth] = useState(0); // mail input의 상태(예:disable) 관리를 위한 state
  const [mailAuthCode, setMailAuthCode] = useState(null); // 서버에서 날아온 인증번호를 담는 용도의 state
  const [mailAuthInput, setMailAuthInput] = useState(""); // 사용자가 입력한 인증번호를 담는 용도의 state

  const [time, setTime] = useState(300); // 시간 300초로 설정하기
  const timerRef = useRef(null); // 시간을 state로만 관리하면 set으로 랜더링할때마다 시간이 깜빡깜빡하는데 화면 랜더링에 영향 없이 타이머(시간)를 담는 용도

  const sendMail = () => {
    if (member.memberEmail === "") {
      Swal.fire({
        icon: "warning",
        title: "이메일 입력",
        text: "이메일을 먼저 입력해주세요.",
      });
      return;
    }

    setTime(300);
    setMailAuthCode(null);
    if (timerRef.current) window.clearInterval(timerRef.current);

    setMailAuth(1);

    Swal.fire({ title: "메일 발송 중...", didOpen: () => Swal.showLoading() });

    axiosInstance
      .post(`/auth/email-verification`, {
        memberEmail: member.memberEmail,
      })
      .then((res) => {
        Swal.fire({
          icon: "success",
          title: "발송 완료",
          text: "이메일로 인증번호가 발송되었습니다.",
        });
        console.log(res.data);
        setMailAuthCode(res.data);
        setMailAuth(2);

        // 인증코드 인증시간이 넘었을때를 위해
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

  // 분 : 초 세팅
  const showTime = () => {
    const min = Math.floor(time / 60);
    const sec = String(time % 60).padStart(2, "0");
    return `${min}:${sec}`;
  };

  // 최종 가입 전 검사 로직 - 위에서부터 순서대로 체크해서 alert띄우기
  const joinMember = () => {
    // checkId가 1이어야 정규식도 맞고 중복여부도 가능한 상태
    if (checkId !== 1) {
      Swal.fire({
        icon: "warning",
        title: "확인 필요",
        text: "아이디 중복 체크 및 형식을 확인해주세요.",
      });
      return;
    }
    // 비밀번호도 완벽하게 일치(1)할 때만 통과
    if (checkPw !== 1) {
      Swal.fire({
        icon: "warning",
        title: "확인 필요",
        text: "비밀번호 형식과 일치 여부를 확인해주세요.",
      });
      return;
    }
    // checkName이 1이어야 중복여부 가능한 상태
    if (checkName !== 1) {
      Swal.fire({
        icon: "warning",
        title: "확인 필요",
        text: "닉네임 중복 체크를 확인해주세요.",
      });
      return;
    }
    if (member.memberName === "") {
      Swal.fire({
        icon: "warning",
        title: "입력 오류",
        text: "이름(닉네임)을 입력하세요.",
      });
      return;
    }
    // mailAuth => 0 : 대기중 (인증전) / 1 : 발송중 / 2 : 입력 대기 / 3 : 인증 성공
    if (mailAuth !== 3) {
      Swal.fire({
        icon: "warning",
        title: "인증 필요",
        text: "이메일 인증을 완료해주세요.",
      });
      return;
    }
    // 주소가 비어있다면
    if (member.memberAddr === "") {
      Swal.fire({
        icon: "warning",
        title: "입력 오류",
        text: "주소를 입력해주세요.",
      });
      return;
    }

    axiosInstance
      .post(`/auth`, member)
      .then((res) => {
        if (
          res.data.memberId === member.memberId &&
          res.data.memberName === member.memberName
        ) {
          Swal.fire({
            icon: "success",
            title: "가입 완료!",
          });
          navigate("/login");
        }
      })
      .catch((err) => {
        console.log(err);
      });
  };

  return (
    <section className={styles.join_section}>
      <div className={styles.join_card}>
        <h3 className={styles.page_title}>회원가입</h3>

        <form
          onSubmit={(e) => {
            e.preventDefault();
            joinMember();
          }}
          autoComplete="off"
        >
          <div className={styles.form_group}>
            <label htmlFor="memberId" className={styles.label}>
              아이디
            </label>
            <div className={styles.input_row}>
              <input
                type="text"
                name="memberId"
                id="memberId"
                placeholder="ID를 입력하세요."
                value={member.memberId}
                onChange={inputMember}
                onBlur={idDupCheck}
              />
            </div>
            {checkId > 0 && (
              <p
                className={`${styles.validation_msg} ${
                  checkId === 1 ? styles.valid : styles.invalid
                }`}
              >
                {checkId === 1 && "사용 가능한 아이디 입니다."}
                {checkId === 2 && "이미 사용중인 아이디 입니다."}
                {checkId === 3 &&
                  "아이디는 영문(필수)과 숫자/특수문자(선택)로 6자 이상이어야 합니다."}
              </p>
            )}
          </div>

          <div className={styles.form_group}>
            <label htmlFor="memberPw" className={styles.label}>
              비밀번호
            </label>
            <div className={`${styles.input_row} ${styles.pw_input_wrap}`}>
              <input
                type={pwVisible ? "text" : "password"}
                name="memberPw"
                id="memberPw"
                placeholder="비밀번호를 입력하세요."
                value={member.memberPw}
                onChange={inputMember}
                onBlur={pwDupCheck}
              />
              {pwVisible ? (
                <span
                  className={`material-icons ${styles.pw_icon}`}
                  onClick={() => setPwVisible(false)}
                >
                  visibility_off
                </span>
              ) : (
                <span
                  className={`material-icons ${styles.pw_icon}`}
                  onClick={() => setPwVisible(true)}
                >
                  visibility
                </span>
              )}
            </div>
            {checkPw === 3 && (
              <p className={`${styles.validation_msg} ${styles.invalid}`}>
                비밀번호는 영문, 특수문자(필수)와 숫자(선택)로 8자 이상이어야
                합니다.
              </p>
            )}
          </div>

          <div className={styles.form_group}>
            <label htmlFor="memberPwRe" className={styles.label}>
              비밀번호 확인
            </label>
            <div className={`${styles.input_row} ${styles.pw_input_wrap}`}>
              <input
                type={pwReVisible ? "text" : "password"}
                name="memberPwRe"
                id="memberPwRe"
                placeholder="비밀번호를 다시 입력하세요."
                value={memberPwRe}
                onChange={(e) => setMemberPwRe(e.target.value)}
                onBlur={pwDupCheck}
              />
              {pwReVisible ? (
                <span
                  className={`material-icons ${styles.pw_icon}`}
                  onClick={() => setPwReVisible(false)}
                >
                  visibility_off
                </span>
              ) : (
                <span
                  className={`material-icons ${styles.pw_icon}`}
                  onClick={() => setPwReVisible(true)}
                >
                  visibility
                </span>
              )}
            </div>
            {(checkPw === 1 || checkPw === 2) && (
              <p
                className={`${styles.validation_msg} ${
                  checkPw === 1 ? styles.valid : styles.invalid
                }`}
              >
                {checkPw === 1 && "비밀번호가 일치합니다."}
                {checkPw === 2 && "비밀번호가 일치하지 않습니다."}
              </p>
            )}
          </div>

          <div className={styles.form_group}>
            <label htmlFor="memberName" className={styles.label}>
              이름 (닉네임)
            </label>
            <div className={styles.input_row}>
              <input
                type="text"
                name="memberName"
                id="memberName"
                placeholder="이름을 입력하세요."
                value={member.memberName}
                onChange={inputMember}
                onBlur={nameDupCheck}
              />
            </div>
            {checkName > 0 && (
              <p
                className={`${styles.validation_msg} ${
                  checkName === 1 ? styles.valid : styles.invalid
                }`}
              >
                {checkName === 1 && "사용 가능한 이름 입니다."}
                {checkName === 2 && "이미 사용중인 이름 입니다."}
              </p>
            )}
          </div>

          <div className={styles.form_group}>
            <label htmlFor="memberEmail" className={styles.label}>
              이메일 (E-Mail)
            </label>
            <div className={styles.input_row}>
              <input
                type="email"
                name="memberEmail"
                id="memberEmail"
                placeholder="이메일을 입력하세요."
                value={member.memberEmail}
                onChange={inputMember}
                readOnly={mailAuth === 1 || mailAuth === 3}
              />
              <button
                type="button"
                className="btn primary sm"
                onClick={sendMail}
                disabled={mailAuth === 1 || mailAuth === 3}
              >
                {mailAuth >= 2 ? "재전송" : "인증 코드 전송"}
              </button>
            </div>
          </div>

          {mailAuth > 1 && (
            <div className={styles.form_group}>
              <label htmlFor="mailAuthInput" className={styles.label}>
                이메일 (E-Mail) - 인증코드
              </label>
              <div className={styles.input_row}>
                <div className={styles.timer_wrap}>
                  <input
                    type="text"
                    name="mailAuthInput"
                    id="mailAuthInput"
                    placeholder="이메일에 도착한 인증코드를 입력하세요."
                    value={mailAuthInput}
                    onChange={(e) => setMailAuthInput(e.target.value)}
                    disabled={mailAuth === 3}
                  />
                  {mailAuth !== 3 && (
                    <span className={styles.timer_text}>{showTime()}</span>
                  )}
                </div>

                <button
                  className="btn primary sm"
                  type="button"
                  disabled={mailAuth === 3}
                  onClick={() => {
                    if (
                      mailAuthCode === mailAuthInput &&
                      mailAuthInput !== ""
                    ) {
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
                  }}
                >
                  인증하기
                </button>
              </div>

              {mailAuth === 3 && (
                <p className={`${styles.validation_msg} ${styles.valid}`}>
                  인증되었습니다.
                </p>
              )}
            </div>
          )}

          <div className={styles.form_group}>
            <label htmlFor="memberPostcode" className={styles.label}>
              주소
            </label>
            <div className={styles.input_row}>
              <input
                type="text"
                name="memberAddr"
                id="memberAddr"
                placeholder="도로명주소"
                value={member.memberAddr}
                readOnly={true}
              />
              <button type="button" className="btn primary sm" onClick={open}>
                주소 찾기
              </button>
            </div>
          </div>

          <div className={styles.final_button_wrap}>
            <button type="submit" className="btn primary lg">
              회원 가입 완료
            </button>
          </div>
        </form>
      </div>
    </section>
  );
};

export default SignUp;
