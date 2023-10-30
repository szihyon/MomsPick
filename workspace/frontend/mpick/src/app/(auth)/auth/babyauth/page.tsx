"use client";

import Image from "next/image";
import { Button, Input } from "@nextui-org/react";
import React, { useState } from "react";
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { DatePicker } from "@mui/x-date-pickers/DatePicker";
import dayjs from "dayjs";
import {
  AiFillCaretDown,
  AiFillCaretUp,
  AiFillPlusCircle,
  AiFillMinusCircle,
} from "react-icons/ai";

interface BabyInfo {
  babyName: string;
  birth: string;
  gender: "M" | "F" | "";
  isFolded: boolean;
}

export default function BabyAuth() {
  const [babyList, setBabyList] = useState<BabyInfo[]>([
    { babyName: "", birth: "", gender: "", isFolded: false },
  ]);
  console.log(babyList);

  const babyNameInput = (index: number, name: string) => {
    const newBaby = [...babyList];
    newBaby[index].babyName = name;
    setBabyList(newBaby);
  };

  const babyBirthInput = (index: number, date: dayjs.Dayjs | null) => {
    const newBaby = [...babyList];
    if (date) {
      newBaby[index].birth = date.format("YYYY-MM-DD");
      setBabyList(newBaby);
    }
  };

  const genderSelected = (index: number, gender: "M" | "F") => {
    const newBaby = [...babyList];
    newBaby[index].gender = gender;
    setBabyList(newBaby);
  };

  const addBabyInfo = () => {
    setBabyList([
      ...babyList,
      { babyName: "", birth: "", gender: "", isFolded: false },
    ]);
  };

  const deleteInfo = (index: number) => {
    const newBaby = [...babyList];
    newBaby.splice(index, 1);
    setBabyList(newBaby);
  };

  const handleToggle = (index: number) => {
    const newBaby = [...babyList];
    newBaby[index].isFolded = !newBaby[index].isFolded;
    setBabyList(newBaby);
  };

  return (
    <div>
      <h1
        style={{
          fontSize: "140%",
          fontWeight: "bold",
          marginBottom: "5%",
          textAlign: "center",
        }}
      >
        아이 정보 입력
      </h1>
      <div
        style={{
          display: "flex",
          justifyContent: "flex-end",
          marginRight: "1%",
        }}
      >
        {babyList.length < 5 && (
          <span onClick={addBabyInfo}>
            <AiFillPlusCircle style={{ fontSize: "180%" }} />
          </span>
        )}
      </div>
      {babyList.map((baby, index) => (
        <div key={index}>
          <span
            onClick={() => handleToggle(index)}
            style={{ cursor: "pointer" }}
          >
            {baby.isFolded ? (
              <AiFillCaretUp style={{ fontSize: "150%", marginBottom: "2%" }} />
            ) : (
              <AiFillCaretDown
                style={{ fontSize: "150%", marginBottom: "2%" }}
              />
            )}
          </span>
          {!baby.isFolded && (
            <div>
              <p style={{ marginLeft: "2%", marginBottom: "3%" }}>성별</p>
              <div
                style={{
                  display: "flex",
                  gap: "10%",
                  justifyContent: "center",
                  alignItems: "center",
                  marginBottom: "5%",
                }}
              >
                <Button
                  variant="bordered"
                  style={{
                    width: "30%",
                    height: "100%",
                    padding: "5%",
                    backgroundColor:
                      baby.gender === "M" ? "#D0F0FD" : "transparent",
                  }}
                  onClick={() => genderSelected(index, "M")}
                >
                  <Image
                    src="/boy.png"
                    alt="남자 아기"
                    width={80}
                    height={80}
                  ></Image>
                </Button>
                <Button
                  variant="bordered"
                  style={{
                    width: "30%",
                    height: "100%",
                    padding: "5%",
                    backgroundColor:
                      baby.gender === "F" ? "#FDD0EF" : "transparent",
                  }}
                  onClick={() => genderSelected(index, "F")}
                >
                  <Image
                    src="/girl.png"
                    alt="여자 아기"
                    width={80}
                    height={80}
                  ></Image>
                </Button>
              </div>
              <div style={{ marginLeft: "2%", marginBottom: "5%" }}>
                <p style={{ marginBottom: "3%" }}>아이 별명</p>
                <Input
                  isRequired
                  isClearable
                  label="아이 별명"
                  variant="bordered"
                  className="w-11/12"
                  onValueChange={(name) => babyNameInput(index, name)}
                  value={baby.babyName}
                ></Input>
              </div>

              <div style={{ marginLeft: "2%" }}>
                <p style={{ marginBottom: "3%" }}>태어난 날 (출산 예정일)</p>
                <div className="w-11/12">
                  <LocalizationProvider dateAdapter={AdapterDayjs}>
                    <DatePicker
                      label="생년월일"
                      disableFuture={true}
                      onChange={(newDate: dayjs.Dayjs | null) =>
                        babyBirthInput(index, newDate)
                      }
                      format="YYYY-MM-DD"
                    ></DatePicker>
                  </LocalizationProvider>
                </div>
              </div>

              <span
                onClick={() => deleteInfo(index)}
                style={{
                  display: "flex",
                  justifyContent: "flex-end",
                  marginRight: "1%",
                  marginBottom: "1%",
                }}
              >
                <AiFillMinusCircle style={{ fontSize: "180%" }} />
              </span>
            </div>
          )}
          {index !== babyList.length - 1 && (
            <div style={{ border: "1px solid #333", marginBottom: "5%" }}></div>
          )}
        </div>
      ))}
    </div>
  );
}
