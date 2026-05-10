// create 함수로 "전역 저장소(store)"를 만들 수 있음
// 전역 저장소 → 앱 어디서든 꺼내 쓸 수 있는 공용 서랍장
import { create } from "zustand";
// persist와 createJSONStorage는 저장소 내용을 브라우저에 저장해주는 도구
// persist → 새로고침해도 데이터가 사라지지 않게 해줌
// createJSONStorage → 어디에 저장할지 방법을 알려줌 (여기서는 localStorage).
import { createJSONStorage, persist } from "zustand/middleware";

const useAuthStore = create(
  // persist로 감싸면 → 새로고침해도 서랍장 내용이 유지
  persist(
    // set은 서랍장 안의 내용을 바꿀 때 사용하는 함수
    (set) => ({
      token: null,

      login: ({ token }) => {
        set({ token });
      },

      logout: () => {
        set({ token: null });
      },
    }),

    {
      // 브라우저 localStorage에 "auth-key"라는 이름으로 저장
      // localStorage = 브라우저가 기억하는 메모장 같은 것
      name: "auth-key",
      storage: createJSONStorage(() => localStorage),

      // partialize: 서랍장 안에서 어떤 것만 브라우저에 저장할지 고르는 함수
      partialize: (state) => ({
        token: state.token,
      }),
    },
  ),
);

export default useAuthStore;
