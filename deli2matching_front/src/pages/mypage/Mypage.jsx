import { useNavigate } from "react-router-dom";
import useAuthStore from "../../utils/useAuthStore";

const Mypage = ({}) => {
  const userId = useAuthStore((state) => state.userId);
  const navigate = useNavigate();
  const [memberOut, setMemberOut] = useState(0); // 0: 회원탈퇴X, 1: 회원탈퇴O

  if (userId === null && memberOut === 0) {
    Swal.fire({ title: "로그인 후 이용 가능합니다", icon: "warning" }).then(
      () => {
        navigate("/login");
      },
    );
  } else if (userId === null && memberOut === 1) {
    Swal.fire({ title: "탈퇴 완료", icon: "success" }).then(() => {
      navigate("/");
    });
  }

  return (
    userId && (
      <section className={styles.mypage_wrap}>
        <div className={styles.mypage_aside}>
          <SideMenu />
        </div>
        <div className={styles.mypage_content}>
          <Routes>
            <Route
              path="myinfo"
              element={<MyInfo setMemberOut={setMemberOut} />}
            ></Route>
            <Route path="mydelivery" element={<MyDelivery />}></Route>
          </Routes>
        </div>
      </section>
    )
  );
};

const SideMenu = () => {
  return (
    <div className={styles.side_menu}>
      <NavLink
        className={({ isActive }) => (isActive ? styles.active_menu : "")}
        to="/mypage/myinfo"
      >
        내 정보
      </NavLink>

      <NavLink
        className={({ isActive }) => (isActive ? styles.active_menu : "")}
        to="/mypage/mydelivery"
      >
        나의 참여 모집
      </NavLink>
    </div>
  );
};

export default Mypage;
