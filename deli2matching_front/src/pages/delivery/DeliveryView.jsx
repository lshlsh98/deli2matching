import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import axiosInstance from "../../utils/axios";
import useAuthStore from "../../utils/useAuthStore";
import styles from "./DeliveryView.module.css";
import Swal from "sweetalert2";

const DeliveryView = () => {
  const { postId } = useParams();
  const navigate = useNavigate();

  const userId = useAuthStore((state) => state.userId);
  console.log("userId: ", userId);

  const [post, setPost] = useState(null);

  // 상세 정보 조회
  const fetchPost = () => {
    axiosInstance
      .get(`/delivery/${postId}`)
      .then((res) => setPost(res.data))
      .catch((err) => console.log(err));
  };

  useEffect(() => {
    fetchPost();
  }, [postId]);

  // 방 삭제 (호스트 전용)
  const handleDelete = () => {
    if (!window.confirm("정말 방을 삭제하시겠습니까?")) return;

    axiosInstance
      .delete(`/delivery/${postId}`)
      .then(() => navigate("/"))
      .catch((err) => console.log(err));
  };

  // 참여하기
  const handleJoin = () => {
    if (post.currentMembers > post.targetMembers - 1) {
      Swal.fire({
        icon: "warning",
        title: "참여 불가",
        text: "모집 인원이 모두 찼습니다.",
        confirmButtonText: "확인",
      });

      return;
    }

    axiosInstance
      .post(`/delivery/${postId}/join`)
      .then(() => fetchPost()) // 참여 후 목록 갱신
      .catch((err) => console.log(err));
  };

  // 참여 취소
  const handleLeave = () => {
    axiosInstance
      .delete(`/delivery/${postId}/join`)
      .then(() => fetchPost()) // 취소 후 목록 갱신
      .catch((err) => console.log(err));
  };

  if (!post) return null;

  // 로그인 여부 (userId가 없으면 비로그인)
  const isLoggedIn = !!userId;

  // 현재 유저가 호스트인지 여부
  const isHost = isLoggedIn && post.hostUserId === userId;

  // 현재 유저가 참여 중인지 여부 (participants 배열에 본인 userId가 있는지)
  const isJoined =
    isLoggedIn && post.participants?.some((p) => p.userId === userId);

  // 진행률 (0 ~ 100)
  const progressPercent = Math.round(
    (post.currentMembers / post.targetMembers) * 100,
  );

  // 마감 임박 여부 (10분 이하)
  const isUrgent = post.minutesUntilDeadline <= 10;

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <section className={styles.section}>
          <div className={styles.restaurant_name}>{post.restaurantName}</div>
          <div className={styles.status_row}>
            <div className={styles.member_count}>
              <span className={styles.member_current}>
                {post.currentMembers}
              </span>
              <span className={styles.member_separator}> / </span>
              <span className={styles.member_target}>{post.targetMembers}</span>
              <span className={styles.member_unit}>명</span>
            </div>

            <div className={styles.deadline_info}>
              <div className={styles.deadline_badge} data-urgent={isUrgent}>
                <span className={styles.deadline_text}>
                  {post.minutesUntilDeadline}분 남음
                </span>
              </div>
              <p className={styles.deadline_sub}>최대 인원 충족 시 조기 종료</p>
            </div>
          </div>

          <div className={styles.progress_track}>
            <div
              className={styles.progress_fill}
              style={{ width: `${progressPercent}%` }}
            />
          </div>

          <p className={styles.target_label}>목표 {post.targetMembers}명</p>
        </section>

        <section className={styles.section}>
          <div className={styles.location_box}>
            <div className={styles.location_texts}>
              <p className={styles.location_label}>픽업 장소</p>
              <p className={styles.location_main}>{post.pickupLocation}</p>
              {post.detailLocation && (
                <p className={styles.location_detail}>{post.detailLocation}</p>
              )}
            </div>
          </div>
        </section>

        {post.memo && (
          <section className={styles.section}>
            <h2 className={styles.section_title}>메모</h2>
            <p className={styles.memo_text}>{post.memo}</p>
          </section>
        )}

        <section className={styles.section}>
          <h2 className={styles.section_title}>참여 중인 이웃</h2>
          <ul className={styles.participant_list}>
            {post.participants?.map((p) => (
              <li key={p.nickname} className={styles.participant_item}>
                {p.userId === post.hostUserId && <span>방장 </span>}
                <span className={styles.participant_nickname}>
                  {p.nickname}
                </span>
              </li>
            ))}
          </ul>
        </section>

        <div className={styles.action_area}>
          {!isLoggedIn ? (
            /* 비로그인: 로그인 페이지로 이동 */
            <button
              type="button"
              className={styles.btn_join}
              onClick={() => navigate("/login")}
            >
              로그인 후 참여하기
            </button>
          ) : isHost ? (
            /* 호스트: 방 삭제 버튼 */
            <button
              type="button"
              className={styles.btn_delete}
              onClick={handleDelete}
            >
              방 삭제
            </button>
          ) : isJoined ? (
            /* 일반 유저 + 참여 중: 참여 취소 버튼 */
            <button
              type="button"
              className={styles.btn_leave}
              onClick={handleLeave}
            >
              참여 취소
            </button>
          ) : (
            /* 일반 유저 + 미참여: 참여하기 버튼 */
            <button
              type="button"
              className={styles.btn_join}
              onClick={handleJoin}
            >
              참여하기
            </button>
          )}
        </div>
      </div>{" "}
    </div>
  );
};

export default DeliveryView;
