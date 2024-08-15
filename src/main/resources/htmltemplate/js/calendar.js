"use strict";

class Calendar {
  constructor(targetElement, date) {
    this.targetElement = targetElement;
    this.date = date;
    this.render();
  }

  setDatePickedCallback(callback) {
    this.datePickedCallback = callback;
  }

  getCurrentDate() {
    return this.date;
  }

  render() {
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
    monthTd.innerText = this.date.getFullYear() + "年" + (this.date.getMonth() + 1) + "月";
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
          tdElem.innerText = "" + nth;
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
          tdElem.onclick = this.dateClicked.bind(this);
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
    this.render();
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
    this.render();
  }

  dateClicked(e) {
    const date = e.target.dataset.date;
    this.date.setDate(parseInt(date));
    this.render();
    if (this.datePickedCallback !== undefined && this.datePickedCallback !== null) {
      this.datePickedCallback(this.date);
    }
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
}
