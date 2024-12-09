import os
import sys
import time
from datetime import datetime
import pandas as pd
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver import ActionChains
from webdriver_manager.chrome import ChromeDriverManager


class BaseCrawler:
    _url: str
    _driver: webdriver.Chrome
    _wait: WebDriverWait

    def __init__(self, url, column_mapping):
        self._wait = None
        self._url = url
        self.column_mapping = column_mapping  # Ánh xạ tên cột

    def setup(self):
        chrome_options = Options()
        chrome_options.add_argument("--headless")
        chrome_options.add_argument("--no-sandbox")
        chrome_options.add_argument("--disable-dev-shm-usage")

        self._driver = webdriver.Chrome(
            service=Service(ChromeDriverManager().install()),
            options=chrome_options
        )

    def handle(self):
        self.setup()
        self._driver.get(self._url)
        self._wait = WebDriverWait(self._driver, 10)
        self.swap_stab()
        time.sleep(2)
        self.select_drop_down()
        time.sleep(2)

    def swap_stab(self):
        tabs = self._wait.until(
            EC.presence_of_all_elements_located((By.CLASS_NAME, 'tabs-box'))
        )
        for tab in tabs:
            if "Symbol Liquidation Orders" in tab.text:
                action = ActionChains(self._driver)
                action.move_to_element(tab).click().perform()
                break
        self._wait.until(EC.presence_of_element_located((By.CLASS_NAME, 'ant-table-tbody')))

    def select_drop_down(self):
        dropdowns = self._wait.until(
            EC.presence_of_all_elements_located((By.CLASS_NAME, 'ant-select-selector'))
        )
        dropdowns[14].click()

        items = self._wait.until(
            EC.presence_of_all_elements_located((By.CLASS_NAME, 'ant-select-item-option-content'))
        )

        for item in items:
            if "BTC" in item.text:
                item.click()
                time.sleep(5)
                self.click_load_more()
                self.crawl_order()
                break

    def click_load_more(self):
        scroll_step = 300
        last_height = self._driver.execute_script("return document.body.scrollHeight")
        while True:
            for scroll_position in range(0, last_height, scroll_step):
                self._driver.execute_script(f"window.scrollTo(0, {scroll_position});")
                time.sleep(0.5)
            new_height = self._driver.execute_script("return document.body.scrollHeight")
            if new_height == last_height:
                break
            last_height = new_height

    def crawl_order(self):
        table = self._driver.find_elements(By.CSS_SELECTOR, "table")[5]
        rows = table.find_elements(By.CSS_SELECTOR, "tbody tr.ant-table-row.ant-table-row-level-0")
        data = [self.crawl_row(row) for row in rows]

        # Đổi tên cột theo ánh xạ column_mapping
        df = pd.DataFrame(data)
        df.rename(columns=self.column_mapping, inplace=True)
        file_format = "csv"
        df.to_csv(output_path, index=False, sep=separator)

    def crawl_row(self, row):
        exchange_name = row.find_element(By.CSS_SELECTOR,
                                         "td:nth-child(1) span.avatar-span span:nth-child(2)").text.strip()
        symbol = row.find_element(By.CSS_SELECTOR, "td:nth-child(3) div div div").text.strip()
        amount = row.find_element(By.CSS_SELECTOR, "td:nth-child(4)").text.strip()
        price = row.find_element(By.CSS_SELECTOR, "td:nth-child(5)").text.strip()
        value_price, side = price.replace("\n", " ").strip().replace("$", "").split(" ", 1)
        time_created = row.find_element(By.CSS_SELECTOR, "td:nth-child(2)").text.strip()

        current_year = datetime.now().year
        time_with_year = f"{time_created} {current_year}"
        parsed_time = datetime.strptime(time_with_year, "%d %b %I:%M %p %Y")
        formatted_time = parsed_time.strftime("%Y-%m-%d %H:%M:%S")

        return {
            'symbol': symbol,
            'price': value_price,
            'amount': amount,
            'side': side,
            'time_created': formatted_time,
            'exchange_name': exchange_name
        }


if __name__ == '__main__':
    # print(f'Số lượng parameter truyền vào: {len(sys.argv)}')
    # if len(sys.argv) < 4:
    #     print("Usage: python crawltool2.py <output_path> <separator> <column_mapping>")
    #     sys.exit(1)
    #
    # # Nhận tham số
    # output_path = sys.argv[1]
    # separator = sys.argv[2]
    # column_format = sys.argv[3]

    test_data = [
        "tool",
        "D:/tmp/test_transaction.csv",
        ",",
        "Name,Price,Volume,Side,Time,Exchange Name"
    ]
    if len(test_data) < 4:
        print("Usage: python crawltool2.py <output_path> <separator> <column_mapping>")
        sys.exit(1)

    # Nhận tham số
    output_path = test_data[1]
    separator = test_data[2]
    column_format = test_data[3]

    # Chuyển đổi định dạng cột thành ánh xạ
    column_format_list = column_format.split(",")
    column_mapping = {
        'symbol': column_format_list[0].strip(),
        'price': column_format_list[1].strip(),
        'amount': column_format_list[2].strip(),
        'side': column_format_list[3].strip(),
        'time_created': column_format_list[4].strip(),
        'exchange_name': column_format_list[5].strip()
    }

    try:
        crawler = BaseCrawler("https://coinank.com/liquidation", column_mapping)
        crawler.handle()
        print(f"Crawl completed successfully. Data saved to {output_path}")
    except Exception as e:
        print(f"Error during crawling: {str(e)}")
        sys.exit(1)
