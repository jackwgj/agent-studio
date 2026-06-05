import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { cdnAssetUrl } from '../../../single-spa/assets-url';


@Component({
  selector: 'app-custom-star',
  templateUrl: './custom-star.component.html',
  styleUrls: ['./custom-star.component.less'],
  standalone: true,
})
export class CustomStarComponent implements OnInit {
  @Input("canClick") canClick: boolean = true;
  @Input("iconSize") iconSize: number = 18;
  @Input("score") score: number = 0;
  @Output() childPropertyStarEvent = new EventEmitter<number>();
  protected readonly changeUrl = cdnAssetUrl;

  private copyIcons = [];

  private hoverStarPath: string = "assets/images/common/hover-star.png";
  private starPath: string = "assets/images/common/star.png";

  public icons = [
    {
      path: this.starPath,
      isHover: false,
    },
    {
      path: this.starPath,
      isHover: false,
    },
    {
      path: this.starPath,
      isHover: false,
    },
    {
      path: this.starPath,
      isHover: false,
    },
    {
      path: this.starPath,
      isHover: false,
    }
  ];

  ngOnInit() {
    if (typeof this.score !== 'number') {
      this.score = 0;
    }
    let score = Math.min(Math.floor(this.score <= 0 ? 0 : this.score), 5);
    for (let index = 0; index < score; index++) {
      this.icons[index].isHover = true;
      this.icons[index].path = this.hoverStarPath;
    }
  }

  public getStyle(iconData: any) {
    if (iconData.isHover) {
      return {
        width: `${this.iconSize}px`,
        cursor: this.canClick ? 'pointer' : '',
      }
    }
    return {
      width: `${this.iconSize}px`,
      height: `${this.iconSize}px`,
      display: 'inline-block',
      'margin-top': `${Math.ceil(this.iconSize / 18) * 5}px`,
      cursor: this.canClick ? 'pointer' : '',
    }
  }

  public handleClickIcon(index: number) {
    if (!this.canClick) {
      return;
    }
    this.icons = this.copyIcons;
    let lastHoverIndex = -1;
    for (let index = 0; index < this.icons.length; index++) {
      if (this.icons[index].isHover) {
        lastHoverIndex = index;
      }
    }
    if (lastHoverIndex >= index) {
      this.icons[index].isHover = true;
      this.icons[index].path = this.hoverStarPath;
    } else {
      this.icons[index].isHover = !this.icons[index].isHover;
      this.icons[index].path = this.icons[index].isHover ? this.hoverStarPath : this.starPath;
    }
    for (let i = 0; i < this.icons.length; i++) {
      if (i < index) {
        // 此时点亮当前索引位置
        if (this.icons[index].isHover) {
          this.icons[i].isHover = this.icons[index].isHover;
          this.icons[i].path = this.icons[index].isHover ? this.hoverStarPath : this.starPath;
        } else {
          // 此时点灭当前索引位置
          this.icons[i].isHover = true;
          this.icons[i].path = this.hoverStarPath;
        }
      } else if (i > index) {
        this.icons[i].isHover = false;
        this.icons[i].path = this.starPath;
      }
    }
    this.score = (index + 1) * 2;
    this.childPropertyStarEvent.emit(this.score);
  }

  public handleClickMouseEnter(index: number) {
    if (!this.canClick) {
      return;
    }
    this.copyIcons = JSON.parse(JSON.stringify(this.icons));
    for (let i = 0; i < this.copyIcons.length; i++) {
      if (i <= index) {
        this.icons[i].isHover = true;
        this.icons[i].path = this.hoverStarPath;
      } else {
        this.icons[i].isHover = false;
        this.icons[i].path = this.starPath;
      }
    }
  }

  public handleClickMouseLeave() {
    if (!this.canClick) {
      return;
    }
    this.icons = JSON.parse(JSON.stringify(this.copyIcons));
  }
}
