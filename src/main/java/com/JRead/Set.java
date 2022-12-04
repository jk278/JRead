package com.JRead;

public final class Set {

        public static int init() {
                return -(SPUtils.get1(Set.SOURCE_INDEX, -1) +1) *50;
        }

        public static final int MAX_AMOUNT = 50;
        // 每日一文，0~49  原理：缓存文章，索引，资源名一一对应，通过查找
        public static final int INDEX_FROM = -1;   // to -50 索引
        public static final int NAME_FROM = -51;    // to -100 名字
        public static final int RULE_FROM = -101;    // to -150 规则
        public static final int WEBSITE_FROM = -151; // to -200 网址
        public static final int ID_FORM = -201;      // to -250 源顺序

        public static final int SOURCE_INDEX = -300;   // 源索引
        public static final int SHOWED_AMOUNT = -322;  // 显示历史数
        public static final int SOURCE_AMOUNT = -333;   // 源数量

        public static final int EDITOR_INDEX = -999; // 笔记索引
        public static final int EDITOR_AMOUNT = -998;   // 笔记数量
        public static final int EDITOR_FROM = -1001; // 笔记内容
        public static final int EDITOR_MAX = 10000; // -1001 -10000 = -11001 笔记名字


        public static String[] titleStrings = new String[]{"^_^","(*^o^*)","(◦˙▽˙◦)","(*^ω^*)","╮(‵▽′)╭","╰(*´︶`*)╯"};

        // drawer 以5开头
        public static final Integer DRAWER_START_OPEN = 51;
        public static final Integer DRAWER_START_CLOSE = 52;
        public static final Integer DRAWER_END_OPEN = 53;
        public static final Integer DRAWER_END_CLOSE = 54;
        // editor  3
        public static final Integer SCROLL_EDITOR_UP = 31;
        public static final Integer SAVE_EDITOR = 32;
        public static final Integer ADD_NEW = 33;
        public static final Integer SET_TEXT_EDITOR = 34;
        // home  4
        public static final Integer SET_ARTICLE = 41;
        public static final Integer SCROLL_HOME_UP = 42;
        // slider  7
        public static final Integer SET_SLIDER_HOME = 71;
        public static final Integer SET_SLIDER_EDITOR = 72;
        public static final Integer SCROLL_SLIDER_UP = 73;
        public static final Integer SET_SLIDER_END = 74;
        // fabMulti  6
        public static final Integer FAB_HOME = 61;
        public static final Integer FAB_EDITOR = 62;
        // change theme 8
        public static final Integer POPUP_UI_DISMISS = 81;

        private Set() {}
}
