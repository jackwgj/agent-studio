import { MessageComponent, StorageService } from '@shared/services/cfdata.service';
import { I18nNamespace } from '@i18n';
import i18next from 'i18next';
import { FormateTimePipe } from 'src/pipes/formate-time.pipe';
import {getLocalStorage} from "./utils";
// 实例化公共时间处理的对象
const formateTimePipe = new FormateTimePipe();

// 复制功能
export function copyToClipboard(text) {
  const tempTextArea = document.createElement('textarea');
  tempTextArea.value = text;
  document.body.appendChild(tempTextArea);
  tempTextArea.select();
  document.execCommand('copy');
  document.body.removeChild(tempTextArea);
  MessageComponent.showSuccess(i18next.t('copy_success'));
}

// 格式化时间
export function formatTime(timeValue) {
  if (!timeValue) return '';
  return formateTimePipe.transform(timeValue);
}

// UTC时间比北京时间慢8小时
// UTC时间转北京时间
export function utcToBeijingTime(utcString) {
  const date = new Date(utcString + 'Z');

  // 加8小时
  const beijingTime = new Date(date.getTime() + 8 * 60 * 60 * 1000);

  const year = beijingTime.getUTCFullYear();
  const month = String(beijingTime.getUTCMonth() + 1).padStart(2, '0');
  const day = String(beijingTime.getUTCDate()).padStart(2, '0');
  const hours = String(beijingTime.getUTCHours()).padStart(2, '0');
  const minutes = String(beijingTime.getUTCMinutes()).padStart(2, '0');
  const seconds = String(beijingTime.getUTCSeconds()).padStart(2, '0');

  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

// 水印功能
interface IWaterMaskProps {
  fontSize?: number // 字体大小
  color?: string // 字体颜色
  rotate?: number // 旋转角度deg
  opacity?: number // 透明度
  space?: number // 相邻文字间距
}

export function waterMaskCanvas (
  element: HTMLElement = document.body,
  top: number = 10,
  textList: string[] = [
    `   AI生成内容仅供参考    `,
    ''
  ],
  params: IWaterMaskProps = {
    opacity: 0.2,
    fontSize: 12,
    color: '#808080',
    rotate: -20,
    space: 8
  }
){

  const userInfo = getLocalStorage('PROMPT_ENGINEERING_ME');
  const userId = userInfo.userId || ''
  const _tip = i18next.t('water_mask_tip',{ns:I18nNamespace.PLATFORM_MANAGEMENT})
  const newTextList = [ `    ${_tip}`,` ${userId}`]
  const space = params.space || 3 // 可通过此数值大小，控制相邻文字的间距。
  const canvas = document.createElement('canvas')
  const fontSize = params.fontSize
  const height = 180
  canvas.width = space * fontSize * 2.3 // 可根据实际效果，修改2.2值
  canvas.height = height + fontSize * 1.8

  const context: any = canvas.getContext('2d')
  context.translate(0, canvas.height / 1.8)
  context.rotate((params.rotate * Math.PI) / height)
  context.font = `${fontSize}px Vedana`
  context.fillStyle = params.color
  newTextList.forEach((text, index) => {
    context.fillText(text, 0, canvas.height / 2 - 120 + index * 20)
  })
  // 删除水印
  const existingWatermark = document.getElementById('watermark-container');
  if (existingWatermark) {
    existingWatermark.remove();
  }
  // 创建水印
  const div = document.createElement('div')
  div.id = 'watermark-container';
  div.style.pointerEvents = 'none'
  div.style.position = 'absolute'
  div.style.zIndex = '1'
  div.style.top = top + 'px'
  div.style.left = '12px'
  div.style.right = '12px'
  div.style.opacity = String(params.opacity)
  div.style.width = `calc(100% - 24px`
  div.style.height = `calc(100% - ${top}px`
  div.style.background = `url(${canvas.toDataURL('images/png')}) repeat left top`
  element?.appendChild(div)
}


export function isMobile() {
  // UA检测
  const uaMatch = /Android|iPhone|iPad|iPod|BlackBerry|webOS|Windows Phone/i.test(navigator.userAgent);

  // 屏幕宽度检测（常见移动端阈值）
  const isSmallScreen = window.innerWidth <= 768;

  // 触摸支持检测
  const hasTouch = 'ontouchstart' in window || navigator.maxTouchPoints > 0;

  return uaMatch && (isSmallScreen || hasTouch);
}

/**
 * 对于资料解耦的词条链接，在url返回为空时，应该不显示跳转避免链接为404，所以需要将词条写死的a标签去除进行替换。
 */
export function getLinkForNoUrl(url:string, urlTip: string): string{
  if (!url) {
    return urlTip.replace(/<a[^>]*>([^<]+)<\/a>/, '$1');
  }
  return urlTip;
}
