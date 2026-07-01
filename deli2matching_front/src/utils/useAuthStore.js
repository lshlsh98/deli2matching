import { create } from "zustand";
import { createJSONStorage, persist } from "zustand/middleware";

const useAuthStore = create(
  persist(
    (set) => ({
      userId: null,
      memberId: null,
      memberName: null,
      memberEmail: null,
      memberAddr: null,
      token: null,

      login: ({
        userId,
        memberId,
        memberName,
        memberEmail,
        memberAddr,
        token,
      }) => {
        set({ userId, memberId, memberName, memberEmail, memberAddr, token });
      },

      logout: () => {
        set({
          userId: null,
          memberId: null,
          memberName: null,
          memberEmail: null,
          memberAddr: null,
          token: null,
        });
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
        userId: state.userId,
        memberId: state.memberId,
        memberName: state.memberName,
        memberEmail: state.memberEmail,
        memberAddr: state.memberAddr,
      }),
    },
  ),
);

export default useAuthStore;
