import { Injectable } from '@angular/core';
import { getDayOfWeekLabel } from "@routes/agent-center/app-agent/common-logic-agent";
import i18next from "i18next";
import { I18nNamespace } from "@i18n";

@Injectable({
  providedIn: 'root'
})
export class ToolListService {

  constructor() { }

  public convertFromCronExpression(cronExpression: string): string {
    if (cronExpression.indexOf('*/') < 0) {
      const parts = cronExpression.split(' ');
      const seconds = parts[0].padStart(2, '0');
      const min = parts[1].padStart(2, '0');
      const hour = parts[2].padStart(2, '0');
      const day = parts[3];
      const month = parts[4];
      const dayOfWeek = parts[5];
      // 每日
      if (day === '*' && month === '*' && dayOfWeek === '?') {
        const newTime = `${hour}:${min}:${seconds}`;
        return i18next.t(`${I18nNamespace.AGENT_CENTER}:toollistcomponent_69`, {
          newTime: newTime,
        })
      }
      // 每周
      if (day === '?' && month === '*' && dayOfWeek !== '*') {
        const newTime = `${hour}:${min}:${seconds}`;
        const day = getDayOfWeekLabel(dayOfWeek);
        return i18next.t(`${I18nNamespace.AGENT_CENTER}:toollistcomponent_70`, {
          newTime: newTime,
          day: day,
        });
      }

      const intervalMatch = day.match(/^\*\/(\d+)$/);
      const dayIsNumeric = day.match(/^\d+$/);
      // 每月
      if (day !== '*' && dayIsNumeric && month === '*' && dayOfWeek === '?') {
        const newTime = `${hour}:${min}:${seconds}`;
        return i18next.t(`${I18nNamespace.AGENT_CENTER}:toollistcomponent_71`, {
          newTime: newTime,
          day: day,
        });
      }
    } else {
      const parts = cronExpression.split(' ');
      const seconds = parts[0];
      const min = parts[1];
      const hour = parts[2];
      const day = parts[3];

      if (day.indexOf('*/') >= 0) {
        return i18next.t(`${I18nNamespace.AGENT_CENTER}:toollistcomponent_72`, {
          day: day[2],
        });
      }
      if (hour.indexOf('*/') >= 0) {
        return i18next.t(`${I18nNamespace.AGENT_CENTER}:toollistcomponent_73`, {
          hour: hour[2],
        });
      }
      if (min.indexOf('*/') >= 0) {
        return i18next.t(`${I18nNamespace.AGENT_CENTER}:toollistcomponent_74`, {
          min: min[2],
        });
      }
      if (seconds.indexOf('*/') >= 0) {
        return i18next.t(`${I18nNamespace.AGENT_CENTER}:toollistcomponent_75`, {
          seconds: seconds[2],
        });
      }
    }
    throw new Error(i18next.t(`${I18nNamespace.AGENT_CENTER}:toollistcomponent_68`));
  }
}
