import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class ScrollService {
  scrollTop;
  containerID = 'J_container';

  saveScrollTop() {
    const container = document.getElementById(this.containerID);
    if (container) {
      this.scrollTop = container.scrollTop;
    }
  }

  recoverScrollTop() {
    const container = document.getElementById(this.containerID);
    if (container && !isNaN(this.scrollTop)) {
      setTimeout(() => {
        container.scrollTop = this.scrollTop;
      }, 0);
    }
  }
}
