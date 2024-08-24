"use strict";

class Calendar {
  constructor(targetElement, date, pageType) {
    this.targetElement = targetElement;
    this.date = date;

    if (pageType === "TOP") {
      const match = window.location.pathname.match(/^\/\w+\/(\w+)$/);
      this.blogUrl = "./" + match[1] + "/";
    } else if (pageType === "POST") {
      this.blogUrl = "../";
    } else if (pageType === "DATE") {
      this.blogUrl = "./";
    }
    this.getDataAndRender();
  }

  getDataAndRender() {
    const month = Calendar.#createMonthStr(this.date);

    fetch(this.blogUrl + "api/getpostcounteachday?month=" + month, {
      method: "GET"
    })
    .then(response => {
      return response.text();
    })
    .then(ret => {
      this.render(JSON.parse(ret));
    });
  }

  render(postCountData) {
    while (this.targetElement.firstChild ){
      this.targetElement.removeChild(this.targetElement.firstChild);
    }
    const firstYoubi = Calendar.#getFirstYoubi(this.date);
    let nth = 0; // 月の中の何日目かを示す
    let lastNth = Calendar.#getLastNth(this.date);
    let endFlag = false;

    const tableElem = document.createElement("table");
    const headTr = document.createElement("tr");

    const leftArrowTd = document.createElement("td");
    leftArrowTd.innerText = "≪";
    leftArrowTd.classList.add("calendar-left-arrow");
    headTr.appendChild(leftArrowTd);

    const monthTd = document.createElement("td");
    monthTd.colSpan = 5;
    const monthAElem = document.createElement("a");
    monthAElem.href = this.blogUrl + Calendar.#createMonthStr(this.date);
    monthAElem.innerText = this.date.getFullYear() + "年" + (this.date.getMonth() + 1) + "月";
    monthTd.appendChild(monthAElem);
    monthTd.classList.add("calendar-header-month");
    headTr.appendChild(monthTd);

    const rightArrowTd = document.createElement("td");
    rightArrowTd.innerText = "≫";
    rightArrowTd.classList.add("calendar-right-arrow");
    headTr.appendChild(rightArrowTd);
    tableElem.appendChild(headTr);

    leftArrowTd.onclick = this.leftArrowClicked.bind(this);
    rightArrowTd.onclick = this.rightArrowClicked.bind(this);

    for (;;) {
      const trElem = document.createElement("tr");
      tableElem.appendChild(trElem);
      for (let youbi = 0; youbi < 7; youbi++) {
        const tdElem = document.createElement("td");
        trElem.appendChild(tdElem);

        if (nth == 0 && youbi < firstYoubi) {
          ;
        } else if (nth <= lastNth) {
          if (nth == 0 && youbi == firstYoubi) {
            nth = 1;
          }
          const postCountObj = postCountData.find(elem => elem.day === nth);
          if (postCountObj === undefined) {
            tdElem.innerText = "" + nth;
          } else {
            const aElem = document.createElement("a");
            aElem.href = this.blogUrl + Calendar.#createMonthStr(this.date) + ("0" + nth).slice(-2);
            aElem.innerText = "" + nth;
            tdElem.appendChild(aElem);
          }
          tdElem.setAttribute("data-date", nth);
          tdElem.classList.add("calendar-date");
          if (youbi == 0) {
            tdElem.classList.add("calendar-sunday");
          }
          if (youbi == 6) {
            tdElem.classList.add("calendar-saturday");
          }
          if (nth == this.date.getDate()) {
            tdElem.classList.add("calendar-target-date");
          }
          nth++;
          if (nth > lastNth) {
            endFlag = true;
          }
        } else {
          ;
        }
      }
      if (endFlag) {
        break;
      }
    }
    this.targetElement.appendChild(tableElem);
  }

  leftArrowClicked() {
    let newYear = this.date.getFullYear();
    let newMonth;
    let newDate;

    if (this.date.getMonth() == 0) {
      newMonth = 11;
      newYear--;
    } else {
      newMonth = this.date.getMonth() - 1;
    }
    newDate = Calendar.#fixLastDate(newYear, newMonth, this.date.getDate());
    this.date = new Date(newYear, newMonth, newDate);
    this.getDataAndRender();
  }

  rightArrowClicked() {
    let newYear = this.date.getFullYear();
    let newMonth;
    let newDate;

    if (this.date.getMonth() == 11) {
      newMonth = 0;
      newYear++;
    } else {
      newMonth = this.date.getMonth() + 1;
    }
    newDate = Calendar.#fixLastDate(newYear, newMonth, this.date.getDate());
    this.date = new Date(newYear, newMonth, newDate);
    this.getDataAndRender();
  }

  static #getLastNth(date) {
    const date2 = new Date(date.getTime());
    date2.setMonth(date.getMonth() + 1, 0);
    return date2.getDate();
  }

  static #getFirstYoubi(date) {
    const date2 = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    date2.setDate(1);
    return date2.getDay();
  }

  static #fixLastDate(newYear, newMonth, oldDate) {
    const tempDate = new Date(newYear, newMonth, 1);
    const lastNth = Calendar.#getLastNth(tempDate);
    let newDate;

    if (oldDate > lastNth) {
      newDate = lastNth;
    } else {
      newDate = oldDate;
    }

    return newDate;
  }

  static #createMonthStr(date) {
    return date.getFullYear() + ("0" + (date.getMonth() + 1)).slice(-2);
  }
}
