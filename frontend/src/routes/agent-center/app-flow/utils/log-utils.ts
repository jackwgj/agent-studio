export const ALL = 'all';

export function format2ConvTime(timestamp: number): string {
  const date = new Date(timestamp);
  const month = (date.getMonth() + 1).toString().padStart(2, '0');
  const day = date.getDate().toString().padStart(2, '0');
  const hours = date.getHours().toString().padStart(2, '0');
  const minutes = date.getMinutes().toString().padStart(2, '0');
  const seconds = date.getSeconds().toString().padStart(2, '0');
  return `${month}/${day} ${hours}:${minutes}:${seconds}`;
}

export function format2ExecTime(timestamp: number): string {
  const date = new Date(timestamp);
  const hours = date.getHours().toString().padStart(2, '0');
  const minutes = date.getMinutes().toString().padStart(2, '0');
  const seconds = date.getSeconds().toString().padStart(2, '0');
  return `${hours}:${minutes}:${seconds}`;
}

/** 根据时间戳获取本地年月日，不包含时区信息，new Date()会自动算上时区 */
export function getDateByTemp(milliseconds: number): string {
  const time = new Date(milliseconds);
  const year = time.getFullYear();
  const month = String(time.getMonth() + 1).padStart(2, '0');
  const date = String(time.getDate()).padStart(2, '0');

  return `${year}-${month}-${date}`;
}

export function getTimeByDate(date: string): {
  startTime: number;
  endTime: number;
} {
  // 拼接成 ... 00:00:00 ~ ... 23:59:59
  const startDateTime = `${date}T00:00:00`;
  const endDateTime = `${date}T23:59:59`;

  const startDate = new Date(startDateTime);
  const endDate = new Date(endDateTime);

  const startTime = startDate.getTime();
  const endTime = endDate.getTime();

  return {
    startTime,
    endTime,
  };
}

export function addConversationCount(conversationList, dates): void {
  const dateWiseData = {};
  (conversationList || [])?.forEach((item) => {
    const dateString = getDateByTemp(item.start_time);
    if (!dateWiseData[dateString]) {
      dateWiseData[dateString] = {
        totalCount: 0,
        successCount: 0,
        failCount: 0,
      };
    }
    dateWiseData[dateString].totalCount += 1;
    dateWiseData[dateString].successCount += item.success_count || 0;
    dateWiseData[dateString].failCount += item.failure_count || 0;
  });

  dates.forEach((item) => {
    if (item.id === ALL) {
      return;
    }
    const dateValue = dateWiseData[item.id] || {};
    item.successCount = dateValue.successCount || 0;
    item.failCount = dateValue.failCount || 0;
    item.disabled = !dateValue.totalCount;
  });
}

