import { execSync } from 'node:child_process';
import fs from 'node:fs/promises';
import { existsSync } from 'node:fs';
import path from 'node:path';
import process from 'node:process';
import OpenAI from 'openai';

const JA_ROOT = path.join('src', 'content', 'docs', 'ja');
const EN_ROOT = path.join('src', 'content', 'docs');
const ES_ROOT = path.join('src', 'content', 'docs', 'es-419');

function getBaseCommitFromArgs() {
  const args = process.argv.slice(2);
  if (args.length > 0 && !args[0].startsWith('-')) {
    return args[0];
  }
  const envCommit = process.env.DOCS_BASE_COMMIT;
  if (envCommit) {
    return envCommit;
  }
  console.error('Usage: npm run translate:docs -- <base-commit>');
  console.error('Or set DOCS_BASE_COMMIT environment variable.');
  process.exit(1);
}

function getChangedJaFiles(baseCommit) {
  const cmd = `git diff --name-only ${baseCommit} HEAD -- ${JA_ROOT.replace(/\\/g, '/')}`;
  let output;
  try {
    output = execSync(cmd, { encoding: 'utf8' });
  } catch (error) {
    console.error('Failed to execute git diff. Make sure the commit id is valid.');
    console.error(error.message);
    process.exit(1);
  }

  const files = output
    .split('\n')
    .map((line) => line.trim())
    .filter((line) => line.length > 0)
    .map((filePath) => path.normalize(filePath))
    .filter((filePath) => existsSync(filePath));

  return files;
}

function getTargetPaths(jaFilePath) {
  const relativeToJa = path.relative(JA_ROOT, jaFilePath);
  const enPath = path.join(EN_ROOT, relativeToJa);
  const esPath = path.join(ES_ROOT, relativeToJa);
  return { enPath, esPath };
}

function getOpenAIClient() {
  const apiKey = process.env.OPENAI_API_KEY;
  if (!apiKey) {
    console.error('OPENAI_API_KEY environment variable is not set.');
    process.exit(1);
  }
  const model = process.env.DOCS_TRANSLATION_MODEL || 'gpt-4.1-mini';
  const client = new OpenAI({ apiKey });
  return { client, model };
}

async function translateWithOpenAI(client, model, content, targetLanguage, filePath) {
  const languageName =
    targetLanguage === 'en'
      ? 'English'
      : targetLanguage === 'es-419'
      ? 'Latin American Spanish'
      : targetLanguage;

  const systemPrompt =
    'You are a professional technical translator for developer documentation. ' +
    'Translate Japanese Markdown/MDX documentation into the target language. ' +
    'Preserve all Markdown/MDX syntax, code blocks, inline code, links, and frontmatter keys. ' +
    'Translate human-readable text and frontmatter string values, but do not change file structure.';

  const userPrompt =
    `Translate the following Japanese documentation into ${languageName}.\n` +
    'Keep the overall structure and formatting exactly the same.\n\n' +
    '--- BEGIN FILE ---\n' +
    content +
    '\n--- END FILE ---\n';

  const response = await client.chat.completions.create({
    model,
    messages: [
      { role: 'system', content: systemPrompt },
      { role: 'user', content: userPrompt },
    ],
    temperature: 0.2,
  });

  const translated = response.choices[0]?.message?.content;
  if (!translated) {
    throw new Error(`Empty translation result for ${filePath} (${targetLanguage})`);
  }
  return translated.trim();
}

async function translateFile(jaFilePath, client, model) {
  const { enPath, esPath } = getTargetPaths(jaFilePath);

  const jaContent = await fs.readFile(jaFilePath, 'utf8');

  console.log(`Translating ${jaFilePath} -> ${enPath}`);
  const enContent = await translateWithOpenAI(client, model, jaContent, 'en', jaFilePath);
  await fs.mkdir(path.dirname(enPath), { recursive: true });
  await fs.writeFile(enPath, enContent, 'utf8');

  console.log(`Translating ${jaFilePath} -> ${esPath}`);
  const esContent = await translateWithOpenAI(client, model, jaContent, 'es-419', jaFilePath);
  await fs.mkdir(path.dirname(esPath), { recursive: true });
  await fs.writeFile(esPath, esContent, 'utf8');
}

async function main() {
  const baseCommit = getBaseCommitFromArgs();
  console.log(`Base commit: ${baseCommit}`);

  const changedJaFiles = getChangedJaFiles(baseCommit);
  if (changedJaFiles.length === 0) {
    console.log('No updated Japanese docs found under src/content/docs/ja.');
    return;
  }

  console.log('Updated Japanese docs:');
  for (const file of changedJaFiles) {
    console.log(` - ${file}`);
  }

  const { client, model } = getOpenAIClient();

  for (const jaFile of changedJaFiles) {
    try {
      await translateFile(jaFile, client, model);
    } catch (error) {
      console.error(`Failed to translate ${jaFile}:`);
      console.error(error);
    }
  }

  console.log('Translation finished.');
}

main().catch((error) => {
  console.error('Unexpected error during translation:', error);
  process.exit(1);
});

