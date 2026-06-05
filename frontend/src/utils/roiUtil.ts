import { AbstractControl, FormControl } from '@angular/forms';
import { v4 as uuidV4 } from 'uuid';
import { CommonUtils } from './common.util';
import i18next from 'i18next';
interface ROIConfig {
  lines?: {
    data: [number, number][];
    name?: string;
    id?: string;
    properties?: { side1_name: string; side2_name: string; color?: string };
  }[];
  polygons?: {
    data: [number, number][];
    name?: string;
    id?: string;
    properties?: { color?: string };
  }[];
}

export interface ROIAreaConfig {
  id: string;
  name: string;
  color: string;
  title: string;
  colorRaw: { hex: string; r: string; g: string; b: string; a: string };
  data: {
    lines: {
      type: string;
      points: { x: number; y: number }[];
    }[];
    polygons: {
      type: string;
      points: { x: number; y: number }[];
    }[];
    labelLines: {
      type: string;
      points: { x: number; y: number }[];
    }[];
  };
}

export function createNewRoiItem(color?: string) {
  let red = `${CommonUtils.random(255)}`;
  let green = `${CommonUtils.random(255)}`;
  let blue = `${CommonUtils.random(255)}`;

  if (color) {
    [red, green, blue] = color.replace(/[^\d,]/g, '').split(',');
  }

  const roiItem = {
    id: uuidV4(),
    name: '',
    color: `rgba(${red},${green},${blue})`,
    colorRaw: {
      hex: `#${parseInt(red, 16)}${parseInt(green, 16)}${parseInt(blue, 16)}`,
      r: red,
      g: green,
      b: blue,
      a: '1',
    },
    fillColor: '',
    lineColor: '',
    title: i18next.t('roiutil_0'),
    data: {
      lines: [],
      polygons: [],
      labelLines: [],
    },
  };
  roiItem.fillColor = getRGBA(roiItem.colorRaw, [
    undefined,
    undefined,
    undefined,
    0.1,
  ]);
  roiItem.lineColor = getRGBA(roiItem.colorRaw);

  return roiItem;
}

function parseObjectToList(roiObj: ROIConfig): ROIAreaConfig[] {
  const roiList = [];
  if (typeof roiObj !== 'object') {
    return roiList;
  }
  if (roiObj.lines && roiObj.lines instanceof Array) {
    parseLines(roiObj, roiList);
  }

  if (roiObj.polygons && roiObj.polygons instanceof Array) {
    parsePolygons(roiObj, roiList);
  }

  return roiList;
}

function parseLines(roiObj, roiList) {
  roiObj.lines.forEach((lineIterm) => {
    if (!(lineIterm.data && lineIterm.data instanceof Array)) {
      return;
    }
    const points = toPoints(lineIterm.data);
    if (points.length !== 2) {
      return;
    }
    let roiItem = null;
    if (lineIterm.name) {
      const target = roiList.find((item) => {
        return item.name === lineIterm.name;
      });
      if (target) {
        roiItem = target;
      }
    }

    if (!roiItem) {
      roiItem = createNewRoiItem(lineIterm.properties?.color);
      roiItem.name = lineIterm.name || '';
      roiList.push(roiItem);
    }
    if (lineIterm.properties?.side1_name && lineIterm.properties?.side2_name) {
      roiItem.data.labelLines = [
        {
          type: 'label-line',
          points: points,
        },
      ];
    } else {
      roiItem.data.lines = [
        {
          type: 'line',
          points: points,
        },
      ];
    }
  });
}

function parsePolygons(roiObj, roiList) {
  roiObj.polygons.forEach((lineIterm) => {
    if (!(lineIterm.data && lineIterm.data instanceof Array)) {
      return;
    }
    const points = toPoints(lineIterm.data);
    if (points.length < 3) {
      return;
    }
    let roiItem = null;
    if (lineIterm.name) {
      const target = roiList.find((item) => {
        return item.name === lineIterm.name;
      });
      if (target) {
        roiItem = target;
      }
    }

    if (!roiItem) {
      roiItem = createNewRoiItem(lineIterm.properties?.color);
      roiItem.name = lineIterm.name || '';
      roiList.push(roiItem);
    }
    roiItem.data.polygons = [
      {
        type: 'polygon',
        points: points,
      },
    ];
  });
}

export function parseRoiStr(roiStr: string) {
  let list = [];
  try {
    const obj = JSON.parse(roiStr);
    const valid = checkRoiObj(obj);
    if (valid) {
      list = parseObjectToList(obj);
    }
    if (list.length === 0) {
      list.push(createNewRoiItem());
    }
    if (list.length > 5) {
      list.splice(0, list.length - 5);
    }
  } catch (error) {
    list = [createNewRoiItem()];
  }

  list.forEach((item) => {
    setRoiNameControl(item, list);
  });
  return list;
}

export function checkRoiObj(roiObj: {
  lines?: {
    data: [number, number][];
    name?: string;
    id?: string;
    properties?: { side1_name: string; side2_name: string };
  }[];
  polygons?: {
    data: [number, number][];
    id?: string;
    name?: string;
  }[];
}): {
  error_msg: string;
  valid: boolean;
} {
  const errorMsg = {
    format_error: i18next.t('roiutil_1'),
    name_repeat: i18next.t('roiutil_2'),
  };

  const nameDist = {};
  if (typeof roiObj !== 'object') {
    return {
      error_msg: errorMsg.format_error,
      valid: false,
    };
  }

  if (roiObj.lines) {
    if (!(roiObj.lines instanceof Array)) {
      return {
        error_msg: errorMsg.format_error,
        valid: false,
      };
    }
    let error = '';
    const target = roiObj.lines.find((lineIterm) => {
      if (!(lineIterm.data && lineIterm.data instanceof Array)) {
        error = errorMsg.format_error;
        return true;
      }
      const points = toPoints(lineIterm.data);
      if (points.length !== 2) {
        error = errorMsg.format_error;
        return true;
      }
      if (lineIterm.name) {
        if (!nameDist[lineIterm.name]) {
          nameDist[lineIterm.name] = {};
        }
        if (nameDist[lineIterm.name].line) {
          error = errorMsg.name_repeat;
          return true;
        } else {
          nameDist[lineIterm.name].line = true;
        }
      }
      return false;
    });
    if (target) {
      return {
        error_msg: error,
        valid: false,
      };
    }
  }
  if (roiObj.polygons) {
    if (!(roiObj.polygons instanceof Array)) {
      return {
        error_msg: errorMsg.format_error,
        valid: false,
      };
    }
    let error = '';
    const target = roiObj.polygons.find((lineIterm) => {
      if (!(lineIterm.data && lineIterm.data instanceof Array)) {
        error = errorMsg.format_error;
        return true;
      }
      const points = toPoints(lineIterm.data);
      if (points.length < 3) {
        error = errorMsg.format_error;
        return true;
      }
      if (lineIterm.name) {
        if (!nameDist[lineIterm.name]) {
          nameDist[lineIterm.name] = {};
        }
        if (nameDist[lineIterm.name].polygon) {
          error = errorMsg.name_repeat;
          return true;
        } else {
          nameDist[lineIterm.name].polygon = true;
        }
      }
      return false;
    });
    if (target) {
      return {
        error_msg: error,
        valid: false,
      };
    }
  }

  return {
    error_msg: '',
    valid: true,
  };
}

export function checkRoiStr(roiStr: string): {
  error_msg: string;
  valid: boolean;
} {
  try {
    const roiObj = JSON.parse(roiStr);
    return checkRoiObj(roiObj);
  } catch (error) {
    return {
      error_msg: i18next.t('roiutil_3'),
      valid: false,
    };
  }
}

export function parseGraphicsToRoi(
  graphics: ROIAreaConfig[],
  decimal?: number,
) {
  let roiObj = { lines: [], polygons: [] };
  const saveColor = (roiItem, graphicsItem) => {
    if (graphicsItem.color) {
      if (roiItem.properties) {
        roiItem.properties.color = graphicsItem.color;
      } else {
        roiItem.properties = {
          color: graphicsItem.color,
        };
      }
    }
  };

  graphics.forEach((item) => {
    item.data.lines.forEach((line) => {
      const roiItem: any = {
        data: line.points.map((p) => {
          let x = p.x;
          let y = p.y;
          if (typeof decimal === 'number') {
            x = Number(x.toFixed(decimal));
            y = Number(y.toFixed(decimal));
          }
          return [x, y];
        }),
      };

      // name不为空才有name这个字段
      if (item.name) {
        roiItem.name = item.name;
      }

      // color也保存到后端
      saveColor(roiItem, item);
      roiObj.lines.push(roiItem);
    });

    item.data.polygons.forEach((line) => {
      const roiItem: any = {
        data: line.points.map((p) => {
          let x = p.x;
          let y = p.y;
          if (typeof decimal === 'number') {
            x = Number(x.toFixed(decimal));
            y = Number(y.toFixed(decimal));
          }
          return [x, y];
        }),
      };

      // name不为空才有name这个字段
      if (item.name) {
        roiItem.name = item.name;
      }

      // color也保存到后端
      saveColor(roiItem, item);
      roiObj.polygons.push(roiItem);
    });

    item.data.labelLines.forEach((line) => {
      const roiItem: any = {
        data: line.points.map((p) => {
          let x = p.x;
          let y = p.y;
          if (typeof decimal === 'number') {
            x = Number(x.toFixed(decimal));
            y = Number(y.toFixed(decimal));
          }
          return [x, y];
        }),
        properties: {
          side1_name: 'side1_name',
          side2_name: 'side2_name',
        },
      };
      // name不为空才有name这个字段
      if (item.name) {
        roiItem.name = item.name;
      }

      // color也保存到后端
      saveColor(roiItem, item);
      roiObj.lines.push(roiItem);
    });
  });
  if (roiObj.lines.length === 0) {
    delete roiObj.lines;
  }
  if (roiObj.polygons.length === 0) {
    delete roiObj.polygons;
  }

  return JSON.stringify(roiObj);
}

export function setRoiNameControl(roiItem, roiList) {
  (roiItem as any).validation = {
    errorMessage: {
      range: i18next.t('roiutil_2')
    },
  };

  (roiItem as any).nameControl = new FormControl(undefined, [
    (control: AbstractControl): { [key: string]: any } => {
      const value = control.value;

      const target = roiList.find((item) => {
        return item.name && item.id !== roiItem.id && item.name === value;
      });
      if (target) {
        return {
          range: true,
        };
      }

      return {};
    }
  ]);
}

export function getRGBA(colorData, [_r, _g, _b, _a] = []) {
  const r = _r || colorData.r;
  const g = _g || colorData.g;
  const b = _b || colorData.b;
  const a = _a || colorData.a;
  return `rgba(${r},${g},${b},${a})`;
}

export function createPoint(x, y) {
  if (typeof x !== 'number') {
    return null;
  }
  if (typeof y !== 'number') {
    return null;
  }
  return { x: x, y: y };
}

export function toPoints(xyArray) {
  if (!(xyArray instanceof Array)) {
    return [];
  }
  const res = xyArray
    .map((xy) => {
      if (!(xy instanceof Array)) {
        return null;
      }
      return createPoint(xy[0], xy[1]);
    })
    .filter((item) => item);
  return res.length === xyArray.length ? res : [];
}
