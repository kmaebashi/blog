"use strict;"
const footnoteUlArray = document.querySelectorAll("ul.footnote");

for (ul of footnoteUlArray) {
  const liArray = ul.getElementsByTagName("li");

  for (li of liArray) {
    const toAnchor = li.getElementsByTagName("a")[0];
    const name = toAnchor.getAttribute("name");
    const fromAnchor = document.querySelector('[href="' + "#" + name + '"]');
    let titleText = "";
    const textNodes = toAnchor.parentNode.childNodes;
    for (let i = 1; i < textNodes.length; i++) {
      titleText += textNodes[i].textContent;
    }
    fromAnchor.setAttribute("title", titleText);
  }
}
