package com.kmaebashi.blog.service;

import com.kmaebashi.blog.common.Constants;

import java.util.ArrayList;
import java.util.Stack;

public class MarkupConverter {
    private enum Status {
        LINE_HEAD,
        IN_H1,
        IN_H2,
        IN_H3,
        IN_LINE,
        IN_FOOTNOTE
    }

    private enum ListType {
        NOT_LIST,
        UL,
        OL
    }

    private enum Mark {
        H1,
        H2,
        H3,
        QUOTE_START,
        QUOTE_END,
        PRE_START,
        PRE_END,
        LI_UL1,
        LI_UL2,
        LI_UL3,
        LI_OL1,
        LI_OL2,
        LI_OL3,
        FOOTNOTE_START,
        FOOTNOTE_END
    }

    private static class MarkDef {
        Mark type;
        String str;
        String htmlStr;
        boolean lineHead;
        boolean withLf;
        ListType listType;
        int liLevel;

        MarkDef(Mark type, String str, String htmlStr, boolean lineHead, boolean withLf, ListType listType, int liLevel) {
            this.type = type;
            this.str = str;
            this.htmlStr = htmlStr;
            this.lineHead = lineHead;
            this.withLf = withLf;
            this.listType = listType;
            this.liLevel = liLevel;
        }

        MarkDef(Mark type, String str, String htmlStr, boolean lineHead, boolean withLf) {
            this(type, str, htmlStr, lineHead, withLf, ListType.NOT_LIST, 0);
        }
    }

    private static class ListStack {
        ListType type;

        ListStack(ListType type) {
            this.type = type;
        }
    }

    private static MarkDef[] markDef = new MarkDef[] {
        new MarkDef(Mark.H3, "***", "<h4>", true, false),
        new MarkDef(Mark.H2, "**", "<h3>", true, false),
        new MarkDef(Mark.H1, "*", "<h2>", true, false),
        new MarkDef(Mark.QUOTE_START, ">>", "<blockquote>", true, true),
        new MarkDef(Mark.QUOTE_END, "<<", "</blockquote>", true, true),
        new MarkDef(Mark.PRE_START, ">|", "<pre>", true, true),
        new MarkDef(Mark.PRE_END, "|<", "</pre>", true, true),
        new MarkDef(Mark.LI_UL1, "---", "<li>", true, false, ListType.UL, 3),
        new MarkDef(Mark.LI_UL2, "--", "<li>", true, false, ListType.UL, 2),
        new MarkDef(Mark.LI_UL3, "-", "<li>", true, false, ListType.UL, 1),
        new MarkDef(Mark.LI_OL1, "+++", "<li>", true, false, ListType.OL, 3),
        new MarkDef(Mark.LI_OL2, "++", "<li>", true, false, ListType.OL, 2),
        new MarkDef(Mark.LI_OL3, "+", "<li>", true, false, ListType.OL, 1),
        new MarkDef(Mark.FOOTNOTE_START, "((", null,false, false),
        new MarkDef(Mark.FOOTNOTE_END, "))", null,false, false),
    };

    private boolean inParagraph = false;
    private boolean setBr = false;
    private StringBuilder targetSb;
    private ArrayList<StringBuilder> footnoteList = new ArrayList<>();
    private boolean summaryMode;

    public MarkupConverter(boolean summaryMode) {
        this.summaryMode = summaryMode;
    }

    public String convert(String src) {
        this.inParagraph = false;
        this.setBr = false;
        src = src.replace("\\r", "");
        StringBuilder mainSb = new StringBuilder();
        this.targetSb = mainSb;
        StringBuilder currentFootnoteSb = new StringBuilder();
        Stack<ListStack> listStack = new Stack<>();

        Status status = Status.LINE_HEAD;
        int[] markLenBuf = new int[1];
        MarkDef md = null;

        for (int i = 0; i < src.length(); ) {
            switch (status) {
                case LINE_HEAD:
                    md = checkMark(src, i, markLenBuf, true);
                    if (md == null) {
                        if (listStack.size() > 0) {
                            subListLevel(listStack.size(), listStack);
                        }
                        startLine();
                        status = Status.IN_LINE;
                    } else if (md.type == Mark.H1 || md.type == Mark.H2 || md.type == Mark.H3) {
                        endParagraph();
                        writeTag(md.htmlStr);
                        status = md.type == Mark.H1 ? Status.IN_H1
                                : md.type == Mark.H2 ? Status.IN_H2
                                : Status.IN_H3;
                        i += markLenBuf[0];
                    } else if (md.type == Mark.QUOTE_START || md.type == Mark.QUOTE_END
                            || md.type == Mark.PRE_START || md.type == Mark.PRE_END) {
                        endParagraph();
                        writeTag(md.htmlStr + Constants.CRLF);
                        i += markLenBuf[0];
                    } else if (md.type == Mark.LI_UL1 || md.type == Mark.LI_UL2 || md.type == Mark.LI_UL3
                               || md.type == Mark.LI_OL1 || md.type == Mark.LI_OL2 || md.type == Mark.LI_OL3) {
                        endParagraph();
                        if (md.liLevel == listStack.size() + 1) {
                            // レベルが1増えた
                            addListLevel(md, listStack);
                            writeTag(md.htmlStr);
                        } else if (md.liLevel == listStack.size()) {
                            // レベル変わらず
                            if (md.listType != listStack.peek().type) {
                                subListLevel(1, listStack);
                                addListLevel(md, listStack);
                            }
                            writeTag(md.htmlStr);
                        } else if (md.liLevel < listStack.size()) {
                            subListLevel(listStack.size() - md.liLevel, listStack);
                            writeTag(md.htmlStr);
                        } else {
                            writeText("箇条書きの階層が不正です。");
                            return mainSb.toString();
                        }
                        i += markLenBuf[0];
                        status = Status.IN_LINE;
                    } else {
                        assert false : "md.type.." + md.type;
                    }
                    break;
                case IN_H1:
                case IN_H2:
                case IN_H3:
                    if (src.charAt(i) == '\n') {
                        String closeHtml = (status == Status.IN_H1 ? "</h2>"
                                            : status == Status.IN_H2 ? "</h3>"
                                            : "</h4>");
                        writeTag(closeHtml);
                        writeTag(Constants.CRLF);
                        status = Status.LINE_HEAD;
                    } else {
                        writeText(Util.escapeHtmlChar(src.charAt(i)));
                    }
                    i++;
                    break;
                case IN_LINE:
                case IN_FOOTNOTE:
                    md = checkMark(src, i, markLenBuf, false);
                    if (status == Status.IN_LINE && md != null && md.type == Mark.FOOTNOTE_START) {
                        status = Status.IN_FOOTNOTE;
                        i += markLenBuf[0];
                        currentFootnoteSb = new StringBuilder();
                        status = Status.IN_FOOTNOTE;
                    } else if (status == Status.IN_FOOTNOTE && md != null && md.type == Mark.FOOTNOTE_END) {
                        footnoteList.add(currentFootnoteSb);
                        outputFootnoteLink(footnoteList.size());
                        i += markLenBuf[0];
                        currentFootnoteSb = null;
                        status = Status.IN_LINE;

                    } else {
                        StringBuilder targetSb;

                        if (status == Status.IN_LINE) {
                            this.targetSb = mainSb;
                        } else {
                            this.targetSb = currentFootnoteSb;
                        }
                        if (src.charAt(i) == '\n') {
                            this.setBr = true;
                            writeText(Constants.CRLF);
                            status = Status.LINE_HEAD;
                        } else {
                            writeText(Util.escapeHtmlChar(src.charAt(i)));
                        }
                        i++;
                    }
                    break;
                default:
                    assert false : "status.." + status;
            }
        }
        if (listStack.size() > 0) {
            subListLevel(listStack.size(), listStack);
        }

        return mainSb.toString();
    }

    public String getFootnoteStr() {
        StringBuilder sb = new StringBuilder();

        if (footnoteList.size() > 0) {
            sb.append(Constants.CRLF + "<hr>" + Constants.CRLF);
            sb.append("<ul class=\"footnote\">" + Constants.CRLF);
            for (int i = 0; i < footnoteList.size(); i++) {
                sb.append("<li><a name=\"footnote" + (i + 1) + "\">※" + (i + 1) + "</a>"
                        + footnoteList.get(i).toString() + Constants.CRLF);
            }
            sb.append("</ul>" + Constants.CRLF);
            return sb.toString();
        } else {
            return "";
        }
    }

    private void writeText(String str) {
        this.targetSb.append(str);
    }

    private void writeTag(String str) {
        if (!summaryMode) {
            this.targetSb.append(str);
        }
    }

    private MarkDef checkMark(String src, int srcIdx, int[] lenBuf, boolean lineHead) {
        for (MarkDef md : markDef) {
            if (!md.lineHead == lineHead)
                continue;

            int mIdx;
            for (mIdx = 0; mIdx < md.str.length(); mIdx++) {
                if (src.charAt(srcIdx + mIdx) != md.str.charAt(mIdx)) {
                    break;
                }
            }
            if (mIdx == md.str.length()) {
                if (md.withLf) {
                    lenBuf[0] = md.str.length() + 1;
                } else {
                    lenBuf[0] = md.str.length();
                }
                return md;
            }
        }
        return null;
    }

    private void outputFootnoteLink(int number) {
        writeTag("<a href=\"#footnote" + number + "\">※" + number + "</a></sup>");
    }

    private void startLine() {
        if (!inParagraph) {
            writeTag("<p>");
            inParagraph = true;
        } else if (setBr) {
            writeTag("<br>" + Constants.CRLF);
            setBr = false;
        }
    }

    private void endParagraph() {
        if (inParagraph) {
            writeTag("</p>" + Constants.CRLF);
            inParagraph = false;
            setBr = false;
        }
    }

    private void addListLevel(MarkDef md, Stack<ListStack> listStack) {
        if (md.listType == ListType.UL) {
            writeTag("<ul>" + Constants.CRLF);
        } else {
            assert md.listType == ListType.OL : "listType.." + md.listType;
            writeTag("<ol>" + Constants.CRLF);
        }
        listStack.push(new ListStack(md.listType));
    }

    private void subListLevel(int count, Stack<ListStack> listStack) {
        for (int i = 0; i < count; i++) {
            ListStack st = listStack.pop();
            if (st.type == ListType.UL) {
                writeTag("</ul>" + Constants.CRLF);
            } else {
                assert st.type == ListType.OL : "listType.." + st.type;
                writeTag("</ol>" + Constants.CRLF);
            }
        }
    }
}
