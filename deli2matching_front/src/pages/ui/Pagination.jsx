import styles from "./Pagination.module.css";

const Pagination = ({ page, setPage, totalPage, naviSize }) => {
  if (totalPage === null || totalPage < 1) {
    //db 먼저 수행할 수 있도록 함
    return;
  }

  // 현재 페이지 번호 (서버에 주는 숫자 + 1, 1부터 시작하도록)
  const current = page + 1;
  const halfLenth = Math.floor(naviSize / 2);
  // 현재 페이지가 페이지네이션의 가운데 숫자가 될 수 있도록 함
  let startPage = Math.max(1, current - halfLenth);
  // 마지막 페이지가 총 페이지 개수를 넘지 않도록
  let endPage = Math.min(totalPage, startPage + naviSize - 1);

  const pages = new Array();
  for (let i = startPage; i < endPage + 1; i++) {
    pages.push(i);
  }

  const isFirst = current === 1;
  const isLast = current === totalPage;

  return (
    <div className={styles.pagination_wrap}>
      <button
        onClick={() => {
          setPage(0);
        }}
        disabled={isFirst}
      >
        {"<<"}
      </button>
      <button
        onClick={() => {
          setPage(page - 1);
        }}
        disabled={isFirst}
      >
        {"<"}
      </button>
      {pages.map((p, i) => {
        return (
          <button
            key={"pagination-" + i}
            onClick={() => {
              setPage(p - 1);
            }}
            className={p === current ? styles.active : ""}
          >
            {p}
          </button>
        );
      })}
      <button
        onClick={() => {
          setPage(page + 1);
        }}
        disabled={isLast}
      >
        {">"}
      </button>
      <button
        onClick={() => {
          setPage(totalPage - 1);
        }}
        disabled={isLast}
      >
        {">>"}
      </button>
    </div>
  );
};

export default Pagination;
