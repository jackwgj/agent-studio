enum Method {
  get = 'get',
  post = 'post',
}

enum BodyType {
  none = 'none',
  rawText = 'rawText',
  formData = 'formData',
  json = 'json',
}

interface HttpNodeType {
  title: string;
  desc: string;
  method: string;
  url: string;
  headers: string;
  params: any;
  body: any;
}

export const parseCurl = (
  curlCommand: string,
): { node: HttpNodeType | null; error: string | null } => {
  if (!curlCommand.trim().toLowerCase().startsWith('curl')) {
    return {
      node: null,
      error: 'Invalid cURL command. Command must start with "curl".',
    };
  }

  const node: Partial<HttpNodeType> = {
    title: 'HTTP Request',
    desc: 'Imported from cURL',
    method: undefined,
    url: '',
    headers: '',
    params: '',
    body: { type: BodyType.none, data: '' },
  };
  const args = curlCommand.match(/(?:[^\s"']+|"[^"]*"|'[^']*')+/g) || [];
  let hasData = false;

  for (let i = 1; i < args.length; i++) {
    const arg = args[i].replace(/^['"]|['"]$/g, '');
    switch (arg) {
      case '-X':
      case '--request':
        if (i + 1 >= args.length) {
          return {
            node: null,
            error: 'Missing HTTP method after -X or --request.',
          };
        }

        node.method =
          (args[++i].replace(/^['"]|['"]$/g, '') as Method) || Method.get;
        hasData = true;
        break;
      case '-H':
      case '--header':
        if (i + 1 >= args.length) {
          return {
            node: null,
            error: 'Missing header value after -H or --header.',
          };
        }

        node.headers +=
          (node.headers ? '\n' : '') + args[++i].replace(/^['"]|['"]$/g, '');
        break;
      case '-d':
      case '--data':
      case '--data-raw':
      case '--data-binary': {
        if (i + 1 >= args.length) {
          return {
            node: null,
            error:
              'Missing data value after -d, --data, --data-raw, or --data-binary.',
          };
        }

        const bodyPayload = [
          {
            type: 'text',
            value: args[++i].replace(/^['"]|['"]$/g, ''),
          },
        ];
        node.body = { type: BodyType.rawText, data: bodyPayload };
        break;
      }
      case '-F':
      case '--form': {
        if (i + 1 >= args.length) {
          return { node: null, error: 'Missing form data after -F or --form.' };
        }

        if (node.body?.type !== BodyType.formData) {
          node.body = { type: BodyType.formData, data: '' };
        }

        const formData = args[++i].replace(/^['"]|['"]$/g, '');
        const [key, ...valueParts] = formData.split('=');
        if (!key) {
          return { node: null, error: 'Invalid form data format.' };
        }

        let value = valueParts.join('=');

        // To support command like `curl -F "file=@/path/to/file;type=application/zip"`
        // the `;type=application/zip` should translate to `Content-Type: application/zip`
        const typeMatch = value.match(/^(.+?);type=(.+)$/);
        if (typeMatch) {
          const [, actualValue, mimeType] = typeMatch;
          value = actualValue;
          node.headers += `${
            node.headers ? '\n' : ''
          }Content-Type: ${mimeType}`;
        }

        node.body.data += `${node.body.data ? '\n' : ''}${key}:${value}`;
        break;
      }
      case '--json':
        if (i + 1 >= args.length) {
          return { node: null, error: 'Missing JSON data after --json.' };
        }
        node.body = {
          type: BodyType.json,
          data: args[++i].replace(/^['"]|['"]$/g, ''),
        };
        break;
      default:
        if (arg.startsWith('http') && !node.url) {
          node.url = arg;
        }
        break;
    }
  }

  // Determine final method
  node.method = node.method || (hasData ? Method.post : Method.get);

  if (!node.url) {
    return { node: null, error: 'Missing URL or url not start with http.' };
  }

  // Extract query params from URL
  const urlParts = node.url?.split('?') || [];
  if (urlParts.length > 1) {
    node.url = urlParts[0];
    node.params = urlParts[1].replace(/&/g, '\n').replace(/=/g, ': ');
  }

  return { node: node as HttpNodeType, error: null };
};
