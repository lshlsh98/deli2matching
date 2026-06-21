import Box from "@mui/material/Box";
import MenuItem from "@mui/material/MenuItem";
import FormControl from "@mui/material/FormControl";
import Select from "@mui/material/Select";
import { useState, useEffect } from "react";

const BasicSelect = ({ state, setState, list, onChange }) => {
  const [open, setOpen] = useState(false);
  const [minWidth, setMinWidth] = useState(100); // 기본 최소 width

  useEffect(() => {
    // Canvas로 글자 길이 계산
    const canvas = document.createElement("canvas");
    const ctx = canvas.getContext("2d");
    ctx.font = "0.8rem tr_r"; // Select와 같은 폰트

    const maxWidth = list.reduce((max, item) => {
      const width = ctx.measureText(item[1]).width;
      return Math.max(max, width);
    }, 0);

    setMinWidth(maxWidth + 50); // padding 여유 포함
  }, [list]);

  return (
    <Box
      sx={{
        display: "inline-block",
        border: "1px solid",
        borderColor: "var(--primary)",
        borderRadius: 1,
        minWidth: minWidth, // 계산된 최소 너비 적용
      }}
    >
      <FormControl size="small" fullWidth>
        <Select
          value={state}
          onChange={(e) => {
            setState(e.target.value);
            onChange?.(e);
          }}
          // onOpen={() => setOpen(true)}
          // onClose={() => setOpen(false)}
          sx={{
            fontSize: "0.8rem",
            fontFamily: "tr_r",
            color: "var(--primary)",
            height: 40,
            "& .MuiSelect-select": {
              padding: "4px 10px",
            },
            "& fieldset": {
              border: "none",
            },
          }}
          MenuProps={{
            PaperProps: {
              sx: {
                minWidth: minWidth, // 드롭다운 메뉴도 동일하게
              },
            },
            disableScrollLock: true,
          }}
        >
          {list.map((item) => (
            <MenuItem
              key={item[0]}
              value={item[0]}
              sx={{
                fontFamily: "tr_r",
                color: "var(--primary)",
                fontSize: "0.8rem",
              }}
            >
              {item[1]}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
    </Box>
  );
};

export default BasicSelect;
