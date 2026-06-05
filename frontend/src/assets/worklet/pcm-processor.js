class PCMProcessor extends AudioWorkletProcessor {
  constructor() {
    super();
    this.buffer = [];
    this.sampleRateTarget = 16000; // 默认采样率
    this.sendInterval = 1; // 每1秒发送一次
    this.inputSampleRate = sampleRate; // 浏览器默认采样率48000Hz
    this.samplesNeeded = this.sampleRateTarget * this.sendInterval;
    this.stop = false;

    this.port.onmessage = (e) => {
      const { sampleRate, timeslice, stop } = e.data || {};
      if (sampleRate) {
        this.sampleRateTarget = sampleRate;
      }
      if (timeslice) {
        this.sendInterval = timeslice / 1000;
      }
      this.samplesNeeded = this.sampleRateTarget * this.sendInterval;
      this.stop = stop ?? false;
    };
  }

  process(inputs) {
    // 主线程中止PCM采集之后将buffer里缓存的数据一次性传出
    if (this.stop) {
      this.postChunk(true);
      this.buffer = [];
      return false;
    }
    const input = inputs[0][0];
    if (!input) return true;

    const downsampled = this.downsampleBuffer(
      input,
      this.inputSampleRate,
      this.sampleRateTarget,
    );
    this.buffer.push(...downsampled);

    if (this.buffer.length >= this.samplesNeeded) {
      this.postChunk();
    }

    return true;
  }

  postChunk(isLast = false) {
    const chunk = this.buffer.slice(0, this.samplesNeeded);
    this.buffer = this.buffer.slice(this.samplesNeeded);

    const pcmBuffer = new Int16Array(chunk.length);
    for (let i = 0; i < chunk.length; i++) {
      const s = Math.max(-1, Math.min(1, chunk[i]));
      pcmBuffer[i] = s < 0 ? s * 0x8000 : s * 0x7fff;
    }

    this.port.postMessage(
      {
        buffer: pcmBuffer.buffer,
        isLast,
      },
      [pcmBuffer.buffer],
    );
  }

  downsampleBuffer(input, inputRate, targetRate) {
    if (targetRate === inputRate) return input;

    const ratio = inputRate / targetRate;
    const outputLength = Math.floor(input.length / ratio);
    const output = new Float32Array(outputLength);

    let inputIndex = 0;
    for (let i = 0; i < outputLength; i++) {
      output[i] = input[Math.floor(inputIndex)];
      inputIndex += ratio;
    }

    return output;
  }
}

registerProcessor('pcm-processor', PCMProcessor);
