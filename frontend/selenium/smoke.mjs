import assert from 'node:assert/strict';
import { Builder, By, until } from 'selenium-webdriver';
import chrome from 'selenium-webdriver/chrome.js';

const baseUrl = process.env.SELENIUM_BASE_URL || 'http://localhost:5173';

const options = new chrome.Options();
if (process.env.SELENIUM_HEADLESS !== 'false') {
  options.addArguments('--headless=new');
}
options.addArguments('--window-size=1400,1000');

let driver;

try {
  driver = await new Builder()
    .forBrowser('chrome')
    .setChromeOptions(options)
    .build();

  await driver.get(baseUrl);

  await driver.wait(
    until.elementLocated(By.xpath("//*[normalize-space()='HiveWatch Lite']")),
    15000,
  );

  await driver.wait(
    until.elementLocated(By.xpath("//*[contains(normalize-space(), 'Hive monitoring dashboard')]")),
    15000,
  );

  await driver.wait(
    until.elementLocated(By.xpath("//*[contains(normalize-space(), 'Hive management')]")),
    15000,
  );

  await driver.wait(
    until.elementLocated(By.xpath("//*[contains(normalize-space(), 'The default API URL is')]")),
    15000,
  );

  const bodyText = await driver.findElement(By.tagName('body')).getText();

  assert.match(bodyText, /HiveWatch Lite/);
  assert.match(bodyText, /Hive monitoring dashboard/);
  assert.match(bodyText, /Hive management/);

  console.log('Selenium smoke test passed.');
} catch (error) {
  console.error('Selenium smoke test failed.');
  console.error(error);
  process.exitCode = 1;
} finally {
  if (driver) {
    await driver.quit();
  }
}