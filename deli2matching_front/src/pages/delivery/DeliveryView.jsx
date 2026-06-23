import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import axiosInstance from "../../utils/axios";
import useAuthStore from "../../utils/useAuthStore";
import styles from "./DeliveryView.module.css";

// DeliveryView: 배달 모집 상세 페이지
const DeliveryView = () => {
  const { postId } = useParams();
  const navigate = useNavigate();

  // 현재 로그인한 유저 닉네임 (host 여부 판단에 사용)
  const nickname = useAuthStore((state) => state.nickname);

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

  // 현재 유저가 호스트인지 여부
  const isHost = post.hostNickname === nickname;

  // 현재 유저가 참여 중인지 여부 (participants 배열에 본인 닉네임이 있는지)
  const isJoined = post.participants?.some((p) => p.nickname === nickname);

  // 진행률 (0 ~ 100)
  const progressPercent = Math.round(
    (post.currentMembers / post.targetMembers) * 100,
  );

  // 마감 임박 여부 (10분 이하)
  const isUrgent = post.minutesUntilDeadline <= 10;

  return (
    <div className={styles.page}>
      {/* ── 현재 모집 현황 섹션 ── */}
      <section className={styles.section}>
        {/* 상단: 인원 현황(좌) + 마감 정보(우) */}
        <div className={styles.status_row}>
          {/* 현재 / 목표 인원 */}
          <div className={styles.member_count}>
            <span className={styles.member_current}>{post.currentMembers}</span>
            <span className={styles.member_separator}> / </span>
            <span className={styles.member_target}>{post.targetMembers}</span>
            <span className={styles.member_unit}>명</span>
          </div>

          {/* 마감 시간 + 안내 문구 */}
          <div className={styles.deadline_info}>
            <div className={styles.deadline_badge} data-urgent={isUrgent}>
              {/* 긴급 여부에 따라 아이콘 색상은 CSS data-urgent로 처리 */}
              <span className={styles.deadline_icon}>🔴</span>
              <span className={styles.deadline_text}>
                비감 {post.minutesUntilDeadline}분 남음
              </span>
            </div>
            <p className={styles.deadline_sub}>최대 인원 충족 시 조기 종료</p>
          </div>
        </div>

        {/* 진행률 바 */}
        <div className={styles.progress_track}>
          <div
            className={styles.progress_fill}
            style={{ width: `${progressPercent}%` }}
          />
        </div>

        {/* 목표 인원 라벨 */}
        <p className={styles.target_label}>목표 {post.targetMembers}명</p>
      </section>

      {/* ── 픽업 장소 섹션 ── */}
      <section className={styles.section}>
        <div className={styles.location_box}>
          {/* 위치 핀 아이콘 */}
          <span className={styles.location_icon}>📍</span>
          <div className={styles.location_texts}>
            <p className={styles.location_label}>픽업 장소</p>
            <p className={styles.location_main}>{post.pickupLocation}</p>
            {/* 상세 장소가 있을 때만 표시 */}
            {post.detailLocation && (
              <p className={styles.location_detail}>{post.detailLocation}</p>
            )}
          </div>
        </div>
      </section>

      {/* ── 메모 섹션 (memo가 있을 때만 표시) ── */}
      {post.memo && (
        <section className={styles.section}>
          <h2 className={styles.section_title}>메모</h2>
          <p className={styles.memo_text}>{post.memo}</p>
        </section>
      )}

      {/* ── 참여 중인 이웃 섹션 ── */}
      <section className={styles.section}>
        <h2 className={styles.section_title}>참여 중인 이웃</h2>
        <ul className={styles.participant_list}>
          {post.participants?.map((p) => (
            <li key={p.nickname} className={styles.participant_item}>
              {/* 호스트에게는 방장 뱃지 표시 */}
              {p.nickname === post.hostNickname && (
                <span className={styles.host_badge}>방장</span>
              )}
              <span className={styles.participant_nickname}>{p.nickname}</span>
            </li>
          ))}
        </ul>
      </section>

      {/* ── 하단 액션 버튼 ── */}
      <div className={styles.action_area}>
        {isHost ? (
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
    </div>
  );
};

export default DeliveryView;
