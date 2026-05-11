import { Link, useNavigate } from "react-router-dom";
import axiosInstance from "./utils/axios";

// SignUp 컴포넌트: 회원가입 페이지 화면
const SignUp = () => {
  const navigate = useNavigate();

  // handleSubmit: 회원가입 버튼을 눌렀을 때 실행되는 함수
  const handleSubmit = (event) => {
    // 기본 동작(페이지 새로고침)을 막음
    event.preventDefault();

    // FormData로 폼 안에 입력된 값들을 가져옴
    const data = new FormData(event.target);
    const username = data.get("username"); // 아이디 입력값
    const password = data.get("password"); // 비밀번호 입력값

    // 서버에 회원가입 요청을 보냄
    // POST /auth/signup → 서버의 회원가입 처리 주소
    axiosInstance
      .post("/auth/signup", { username, password })
      .then(() => {
        navigate("/login");
      })
      .catch((err) => {
        console.log(err);
      });
  };

  return (
    <Container component="main" maxWidth="xs" style={{ marginTop: "8%" }}>
      <form noValidate onSubmit={handleSubmit}>
        <Grid container spacing={2}>
          <Grid item xs={12}>
            <Typography component="h1" variant="h5">
              계정 생성
            </Typography>
          </Grid>
          {/* 아이디 입력란 */}
          <Grid item xs={12}>
            <TextField
              autoComplete="fname"
              name="username"
              variant="outlined"
              required
              fullWidth
              id="username"
              label="아이디"
              autoFocus // 페이지가 열리면 이 입력란에 자동으로 커서가 생겨요.
            />
          </Grid>
          {/* 비밀번호 입력란 */}
          <Grid item xs={12}>
            <TextField
              variant="outlined"
              required
              fullWidth
              name="password"
              label="패스워드"
              type="password" // 입력값이 *로 가려져요.
              id="password"
              autoComplete="current-password"
            />
          </Grid>
          {/* 회원가입 버튼 */}
          <Grid item xs={12}>
            <Button type="submit" fullWidth variant="contained" color="primary">
              계정 생성
            </Button>
          </Grid>
          {/* 이미 계정이 있으면 로그인 페이지로 가는 링크 */}
          <Grid item xs={12}>
            <Link to="/login" variant="body2">
              이미 계정이 있습니까? 로그인 하세요.
            </Link>
          </Grid>
        </Grid>
      </form>
    </Container>
  );
};

// 다른 파일에서 SignUp을 쓸 수 있도록 내보내요.
export default SignUp;
