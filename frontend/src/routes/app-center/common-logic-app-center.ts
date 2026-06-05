import { IFlowChatItem, IFlowChatItemArgs } from '@routes/agent-center/types/common.types';

/**
 * 知识问答类体验页面的通用逻辑
 */
const commonLogic = {
  /** 在首页和聊天页的输入框中，ctrl+enter——换行，enter——发送问题 */
  handleKeyboardEvent(component: any, event: KeyboardEvent) {
    if (event.key === 'Enter') {
      // 需要通过文本api操作编辑区，不能简单的加到结尾
      if (event.ctrlKey || event.altKey) {
        const inputElement = component.chatTextarea?.nativeElement ||
          component.chatTextarea?.current;
        if (!inputElement) return;
        const { selectionStart, selectionEnd } = inputElement;
        inputElement.setRangeText('\n', selectionStart, selectionEnd, 'end');
        const inputEvent = new Event('input', { bubbles: true });
        inputElement.dispatchEvent(inputEvent);
      } else {
        component.sendQuestion();
      }
      event.preventDefault();
    }
  },

  /** 发送新问题时，抽离chatLoop数组每个对象的初始字段值 */
  createChatItem(questionInputed: string) {
    return {
      loading: true,
      showQuestion: questionInputed.trim(),
      showAnswer: undefined,
      showBottomBtns: false,
      isTimeoutOrError: false,
      showCopiedTip: false,
      chunkIds: [],
      passContentReview: true,
      isPopconfirmOpen: false,
      isClickLikeBtn: false,
      isClickDislikeBtn: false,
      isLike: false,
      isDislike: false,
    };
  },

  /** 对话型工作流：发送新问题时，抽离chatLoop数组每个对象的初始字段值 */
  createFlowChatItem(
    config: IFlowChatItemArgs = {
      query: '',
      dialogId: '',
      options: {},
    },
  ): IFlowChatItem {
    const { query, dialogId, options } = config;

    const item = {
      showQuestion: query.trim(),
      showAnswer: [
        {
          text: '',
          loading: true,
          isLike: false,
          isDislike: false,
          isShowInputParams: false,
          isConfirmed: 'init',
          inputList: [],
          uuid: '',
          isFinished: false,

        },
      ],
      cards:[],
      thinking: false,
      thinkValue: '',
      thinkLoading: true,
      collapsed: false, // 默认是展开状态
    };

    return {
      ...item,
      ...(dialogId && { dialogId }),
      ...options,
    };
  },
};

export default commonLogic;
