from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver import ActionChains
import csv
import time
from datetime import datetime
import sys
import io
from webdriver_manager.chrome import ChromeDriverManager

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
# test_data = [
#         "tool",
#         "D:/tmp/20241206_010035.csv",
#         ",",
#         "Name,Price,Volume,Side,Time,Exchange Name"
#     ]
# if len(test_data) > 1:
#     # Lấy tham số từ sys.argv
#     path_file = test_data[1]  # Tham số đầu tiên
#     seperator = test_data[2]  # Tham số thứ hai
#     fileFormat = test_data[3]
#     print(path_file)
#     print(seperator)
#     print(fileFormat)
if len(sys.argv) > 1:
    # Lấy tham số từ sys.argv
    path_file = sys.argv[1]  # Tham số đầu tiên
    seperator = sys.argv[2]  # Tham số thứ hai
    fileFormat = sys.argv[3]
    print(path_file)
    print(seperator)
    print(fileFormat)
else:
    print("Không có tham số nào được truyền vào.")

def write_transactions_to_file(file_path , file_separator, fileFormat, all_transactions):
    with open(file_path, "w", newline="", encoding="utf-8") as file:
        writer = csv.writer(file, delimiter=file_separator)
        writer.writerow(fileFormat.split(","))
        for transaction in all_transactions:
            writer.writerow([
                transaction.name, 
                transaction.price, 
                transaction.volume, 
                transaction.side,
                transaction.time.strftime('%Y-%m-%d %H:%M:%S'), 
                transaction.nameExchange
            ])

class Transaction:
    def __init__(self, name, price, volume, time, side, imgUrl, nameExchange):
        self.name = name
        self.price = price
        self.volume = volume
        self.side = side
        self.imgUrl = imgUrl
        self.nameExchange = nameExchange
        self.time = datetime.strptime(time, '%Y-%m-%d %H:%M:%S')

    def __str__(self):
        return (f"Tên giao dịch: {self.name}\n"
                f"Giá: {self.price}\n"
                f"imgUrl: {self.imgUrl}\n"
                f"NameExchange: {self.nameExchange}\n"
                f"Side: {self.side}\n"
                f"Khối lượng: {self.volume}\n"
                f"Thời gian: {self.time.strftime('%Y-%m-%d %H:%M:%S')}")

    def __eq__(self, other):
        if isinstance(other, Transaction):
            return (self.name == other.name and
                    self.price == other.price and
                    self.side == other.side and
                    self.volume == other.volume and
                    self.imgUrl == other.imgUrl and
                    self.time == other.time)
        return False

# Cấu hình Chrome
chrome_options = Options()
chrome_options.add_argument("--headless")
chrome_options.add_argument("--no-sandbox")
chrome_options.add_argument("--disable-dev-shm-usage")

exchange_logos = {}
all_transactions = []

try:
    driver = webdriver.Chrome(
            service=Service(ChromeDriverManager().install()),
            options=chrome_options
        )
    driver.get('https://www.coinglass.com/LiquidationData')  # Thay bằng URL của bạn

    # Lấy thông tin logo và tên sàn giao dịch
    table_symbol_logo = WebDriverWait(driver, 10).until(
        EC.presence_of_element_located((By.CLASS_NAME, 'cg-style-542elh'))
    )
    symbol_and_logo_elements = table_symbol_logo.find_elements(By.CLASS_NAME, 'symbol-and-logo')
    for element in symbol_and_logo_elements:
        driver.execute_script("arguments[0].scrollIntoView(true);", element)
        img_element = WebDriverWait(element, 10).until(
            EC.presence_of_element_located((By.TAG_NAME, 'img'))
        )
        img_src = WebDriverWait(driver, 10).until(
            lambda d: img_element.get_attribute('src') if img_element.get_attribute('src') else False
        )
        logo_name_element = element.find_element(By.CLASS_NAME, 'symbol-name')
        logo_name_text = logo_name_element.text
        exchange_logos[img_src] = logo_name_text

    # Chọn tùy chọn từ dropdown
    dropdown_button = WebDriverWait(driver, 10).until(
        EC.presence_of_all_elements_located((By.CSS_SELECTOR, '.MuiAutocomplete-popupIndicator'))
    )
    dropdown_button[1].click()
    dropdown_options = WebDriverWait(driver, 10).until(
        EC.presence_of_all_elements_located((By.CSS_SELECTOR, '.MuiAutocomplete-option'))
    )
    for option in dropdown_options:
        if option.text == "BTC":
            action = ActionChains(driver)
            action.move_to_element(option).click().perform()
            break

    div_element = WebDriverWait(driver, 10).until(
        EC.presence_of_element_located((By.CLASS_NAME, 'orderbook'))
    )
    driver.execute_script("arguments[0].scrollIntoView(true);", div_element)
    total_height = driver.execute_script("return arguments[0].scrollHeight", div_element.find_element(By.CSS_SELECTOR, "div"))
    current_height = driver.execute_script("return arguments[0].clientHeight", div_element)
    num_scrolls = total_height // current_height
    last_tran = None

    for i in range(num_scrolls - 4):
        childs = div_element.find_elements(By.CLASS_NAME, 'liq-live-table')
        for clild in childs:
            try:
                transaction_name = clild.find_element(By.CSS_SELECTOR, '.cg-style-1sugwtq').text
                class_attribute = clild.get_attribute('class')
                transaction_side = "Short" if 'liq-liqShort1' in class_attribute.split() else "Long"
                price = clild.find_element(By.CSS_SELECTOR, '.mbh .Number.undefined').get_attribute('innerHTML')
                volume = clild.find_element(By.CSS_SELECTOR, '.MuiBox-root .Number[aria-label]').text
                time_transaction = clild.find_element(By.CSS_SELECTOR, '.MuiBox-root .cg-style-g38gqj').get_attribute('aria-label')
                img_element = div_element.find_element(By.TAG_NAME, 'img')
                img_src = img_element.get_attribute('src')

                transaction = Transaction(
                    name=transaction_name,
                    price=price,
                    volume=volume,
                    side=transaction_side,
                    time=time_transaction,
                    imgUrl=img_src,
                    nameExchange=exchange_logos[img_src]
                )
                last_tran = transaction
                all_transactions.append(transaction)

            except Exception as e:
                print("Lỗi trong khi xử lý giao dịch:", e)

        driver.execute_script("arguments[0].scrollTop += arguments[0].clientHeight;", div_element)
        time.sleep(0.5)
except Exception as e:
    print("Lỗi:", e)
finally:
    driver.quit()
print(all_transactions)
write_transactions_to_file(""+path_file,seperator+"",fileFormat+"" ,all_transactions)
print("Giao dịch đã được lưu vào file CSV thành công.")