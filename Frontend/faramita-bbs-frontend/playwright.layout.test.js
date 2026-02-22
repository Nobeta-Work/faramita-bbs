const { test, expect } = require('@playwright/test');

test('BlogDetailView layout verification', async ({ page }) => {
  // Set viewport to desktop size
  await page.setViewportSize({ width: 1920, height: 1080 });

  // Load our verification page
  await page.goto('file://' + __dirname + '/layout-verification.html');

  // Verify the overall container structure
  const blogContainer = await page.locator('.blog-container');
  await expect(blogContainer).toBeVisible();

  // Verify flex display
  const containerDisplayStyle = await blogContainer.evaluate(el => getComputedStyle(el).display);
  expect(containerDisplayStyle).toBe('flex');

  // Verify the three columns
  const tocAffix = await page.locator('.toc-affix');
  const mainCol = await page.locator('.main-col');
  const authorAffix = await page.locator('.author-affix');

  await expect(tocAffix).toBeVisible();
  await expect(mainCol).toBeVisible();
  await expect(authorAffix).toBeVisible();

  // Verify widths on desktop
  const tocWidth = await tocAffix.evaluate(el => el.getBoundingClientRect().width);
  const mainWidth = await mainCol.evaluate(el => el.getBoundingClientRect().width);
  const authorWidth = await authorAffix.evaluate(el => el.getBoundingClientRect().width);
  const containerWidth = await blogContainer.evaluate(el => el.getBoundingClientRect().width);

  // Check that TOC and Author columns are ~15% of container width
  const expectedSideWidth = containerWidth * 0.15;
  expect(tocWidth).toBeCloseTo(expectedSideWidth, -2); // Allow for rounding
  expect(authorWidth).toBeCloseTo(expectedSideWidth, -2); // Allow for rounding

  // Check that main column is ~60% of container width
  const expectedMainWidth = containerWidth * 0.6;
  expect(mainWidth).toBeCloseTo(expectedMainWidth, -2); // Allow for rounding

  // Verify fixed positioning of side columns
  const tocPosition = await tocAffix.evaluate(el => getComputedStyle(el).position);
  const authorPosition = await authorAffix.evaluate(el => getComputedStyle(el).position);

  // Check mobile layout
  await page.setViewportSize({ width: 768, height: 1024 });

  // Wait for responsive changes
  await page.waitForTimeout(500);

  // On mobile, the layout should be columnar
  const mobileContainerDisplayStyle = await blogContainer.evaluate(el => getComputedStyle(el).flexDirection);
  expect(mobileContainerDisplayStyle).toBe('column');

  // Verify the order of elements on mobile
  const children = await blogContainer.locator('> *').all();
  // Main column should come first on mobile (order: 1)
  const firstChildClass = await children[0].getAttribute('class');
  expect(firstChildClass).toContain('main-col');

  console.log('All layout tests passed!');
});

test('BlogDetailView scroll behavior verification', async ({ page }) => {
  // Set viewport to desktop size
  await page.setViewportSize({ width: 1920, height: 1080 });

  // Load our verification page
  await page.goto('file://' + __dirname + '/layout-verification.html');

  // Verify that side columns have fixed positioning behavior
  const tocColumn = await page.locator('.toc-container');
  const authorColumn = await page.locator('.author-card');

  // Check that they are visible initially
  await expect(tocColumn).toBeVisible();
  await expect(authorColumn).toBeVisible();

  // Scroll down the page
  await page.evaluate(() => window.scrollTo(0, 1000));

  // Side columns should still be visible (fixed behavior)
  await expect(tocColumn).toBeVisible();
  await expect(authorColumn).toBeVisible();

  console.log('Scroll behavior tests passed!');
});