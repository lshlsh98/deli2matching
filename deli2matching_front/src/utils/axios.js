import axios from "axios";
import useAuthStore from "./useAuthStore";

const instance = axios.create({
  baseURL: `${import.meta.env.VITE_API_BASE_URL}`,
});

// 요청(request) 인터셉터:
// 서버에 요청을 보내기 직전에 실행
instance.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;

  // 토큰이 있으면 요청 헤더에 Authorization 항목으로 넣음
  if (token) {
    // config.headers가 이미 있으면 그대로 사용, 없으면 {} 빈 객체로 만들어서 사용
    config.headers = config.headers || {};
    config.headers["Authorization"] = `Bearer ${token}`;
  }

  // 인터셉터는 수정한 요청 설정을 다시 돌려줘야함
  return config;
});

// 응답(response) 인터셉터:
// 서버에서 응답이 왔을 때 실행
instance.interceptors.response.use(
  (response) => response,

  (error) => {
    const status = error.response?.status;

    // 인증 실패 (토큰 만료 등)
    if (status === 401) {
      // 서랍장에서 로그아웃 함수를 꺼내서 토큰을 지움
      useAuthStore.getState().logout();
      // 로그인 페이지로 강제로 이동
      window.location.href = "/login";
    }

    // 권한 없음
    if (status === 403) {
      // alert("접근 권한이 없습니다.");
    }

    // 에러를 다음 단계로 넘김 (각 컴포넌트의 catch에서 받음)
    return Promise.reject(error);
  },
);

export default instance;
