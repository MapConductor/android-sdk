import fs from 'node:fs/promises';
import path from 'node:path';
import OpenAI from 'openai';

const JA_ROOT = path.join('src', 'content', 'docs', 'ja');
const EN_ROOT = path.join('src', 'content', 'docs');
const ES_ROOT = path.join('src', 'content', 'docs', 'es-419');

const FILES = [
  'event/event-handlers.mdx',
  'components/infobubble.mdx',
  'core/mapcameraposition.mdx',
  'experimental/geojson-layer.mdx',
  'mapviewholder/arcgis-2d.mdx',
];

const apiKey = process.env.OPENAI_API_KEY;
if (!apiKey) { console.error('OPENAI_API_KEY not set'); process.exit(1); }
const model = process.env.DOCS_TRANSLATION_MODEL || 'gpt-4.1-mini';
const client = new OpenAI({ apiKey });

async function translate(content, lang, filePath) {
  const langName = lang === 'en' ? 'English' : 'Latin American Spanish';
  const res = await client.chat.completions.create({
    model,
    temperature: 0.2,
    messages: [
      {
        role: 'system',
        content:
          'You are a professional technical translator for developer documentation. ' +
          'Translate Japanese Markdown/MDX documentation into the target language. ' +
          'Preserve all Markdown/MDX syntax, code blocks, inline code, links, and frontmatter keys. ' +
          'Translate human-readable text and frontmatter string values, but do not change file structure.',
      },
      {
        role: 'user',
        content:
          `Translate the following Japanese documentation into ${langName}.\n` +
          'Keep the overall structure and formatting exactly the same.\n\n' +
          '--- BEGIN FILE ---\n' + content + '\n--- END FILE ---\n',
      },
    ],
  });
  const translated = res.choices[0]?.message?.content?.trim();
  if (!translated) throw new Error(`Empty translation for ${filePath} (${lang})`);
  return translated;
}

for (const rel of FILES) {
  const jaPath = path.join(JA_ROOT, rel);
  const enPath = path.join(EN_ROOT, rel);
  const esPath = path.join(ES_ROOT, rel);

  const jaContent = await fs.readFile(jaPath, 'utf8');

  console.log(`[EN] ${rel}`);
  const enContent = await translate(jaContent, 'en', rel);
  await fs.mkdir(path.dirname(enPath), { recursive: true });
  await fs.writeFile(enPath, enContent, 'utf8');

  console.log(`[ES] ${rel}`);
  const esContent = await translate(jaContent, 'es-419', rel);
  await fs.mkdir(path.dirname(esPath), { recursive: true });
  await fs.writeFile(esPath, esContent, 'utf8');
}

console.log('Done.');
