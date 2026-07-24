import { useEffect, useState } from "react";
import styles from "./DeliveryList.module.css";
import axiosInstance from "../../utils/axios";
import { useKakaoPostcode } from "@clroot/react-kakao-postcode";
import BasicSelect from "../ui/BasicSelect";
import Pagination from "../ui/Pagination";
import { useNavigate } from "react-router-dom";
import useAuthStore from "../../utils/useAuthStore";

const DeliveryList = () => {
  const memberAddr = useAuthStore((state) => state.memberAddr);
  const [location, setLocation] = useState(memberAddr || "");
  const [keyword, setKeyword] = useState("");
  const [searchKeyword, setSearchKeyword] = useState("");
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(9);
  const [totalPage, setTotalPage] = useState(null);
  const [order, setOrder] = useState(0); // 0: 최신순 / 1: 시간 적게 남은 순
  const [list, setList] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    axiosInstance
      .get(
        `/delivery?location=${location}&keyword=${keyword}&order=${order}&page=${page}&size=${size}`,
      )
      .then((res) => {
        console.log(res);
        setList(res.data.list ?? []);
        setTotalPage(res.data.totalPage);
      })
      .catch((err) => {
        console.log(err);
      });
  }, [location, keyword, order, page]);

  const { open } = useKakaoPostcode({
    onComplete: (data) => {
      setLocation(data.roadAddress);
    },
  });

  return (
    <div className={styles.list_page}>
      <div className={styles.list_header}>
        <div className={styles.list_header_left}>
          <div className={styles.list_header_location} onClick={open}>
            <span
              className={styles.list_header_location_text}
            >{`현재 주소: ${location}`}</span>
          </div>
          <h1 className={styles.delivery_list_header_title}>모집 중인 배달</h1>
        </div>

        <div className={styles.list_header_right}>
          <BasicSelect
            state={order}
            setState={setOrder}
            list={[
              [0, "최신순"],
              [1, "남은 시간"],
            ]}
          />
          <div className={styles.list_header_search}>
            <input
              type="text"
              className={styles.list_header_search_input}
              placeholder="식당을 검색해보세요"
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
            />
            <span
              className={`material-symbols-outlined ${styles.list_header_search_icon}`}
              onClick={() => {
                setKeyword(searchKeyword);
              }}
            >
              search
            </span>
          </div>
        </div>
      </div>

      <div className={styles.list_grid}>
        {list.map((item) => (
          <DeliveryCard
            key={item.postId}
            postId={item.postId}
            restaurantName={item.restaurantName}
            targetMembers={item.targetMembers}
            currentMembers={item.currentMembers}
            minutesUntilDeadline={item.minutesUntilDeadline}
          />
        ))}

        {list.length === 0 && (
          <p className={styles.list_empty}>검색 결과가 없습니다.</p>
        )}
      </div>
      <div className={styles.regist_btn}>
        <button
          onClick={() => {
            navigate(`/regist`);
          }}
        >
          모집 등록
        </button>
      </div>
      <div className={styles.pagination_section}>
        <Pagination
          totalPage={totalPage}
          page={page}
          setPage={setPage}
          naviSize={5}
        />
      </div>
    </div>
  );
};

const DeliveryCard = ({
  postId,
  restaurantName,
  targetMembers,
  currentMembers,
  minutesUntilDeadline,
}) => {
  const navigate = useNavigate();
  const progressPercent = Math.round((currentMembers / targetMembers) * 100);

  return (
    <div
      className={styles.card}
      onClick={() => {
        navigate(`/view/${postId}`);
      }}
    >
      <h2 className={styles.card_name}>{restaurantName}</h2>

      <div className={styles.card_meta}>
        <span className={styles.card_clock_icon}>&#x23F1;</span>
        <span className={styles.card_time}>{minutesUntilDeadline}분 남음</span>
        <span
          className={styles.card_status_dot}
          data-urgent={minutesUntilDeadline <= 10}
        />
      </div>

      <div className={styles.card_participation}>
        <span className={styles.card_participation_label}>참여 현황</span>

        <div className={styles.card_participation_right}>
          <span className={styles.card_count}>
            {currentMembers}/{targetMembers}
          </span>
        </div>
      </div>

      <div className={styles.card_progress_track}>
        <div
          className={styles.card_progress_fill}
          style={{ width: `${progressPercent}%` }}
        />
      </div>
    </div>
  );
};

export default DeliveryList;
