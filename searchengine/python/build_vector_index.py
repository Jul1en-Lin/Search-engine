import os
import torch
import numpy as np
import faiss
import pymysql
from transformers import AutoTokenizer, AutoModel
from tqdm import tqdm
import json

class VectorIndexBuilder:
    def __init__(self, model_path, db_config, index_save_path):
        """
        初始化向量索引构建器
        :param model_path: text2vec模型路径
        :param db_config: 数据库配置
        :param index_save_path: 索引保存路径
        """
        self.device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
        self.model_path = model_path
        self.db_config = db_config
        self.index_save_path = index_save_path

        # 加载模型和分词器
        print("正在加载模型...")
        self.tokenizer = AutoTokenizer.from_pretrained(model_path)
        self.model = AutoModel.from_pretrained(model_path).to(self.device)
        self.model.eval()

        # 创建索引保存目录
        os.makedirs(index_save_path, exist_ok=True)

    def get_embeddings(self, texts, batch_size=32):
        """
        获取文本的向量表示
        :param texts: 文本列表
        :param batch_size: 批处理大小
        :return: 向量数组
        """
        embeddings = []

        for i in tqdm(range(0, len(texts), batch_size), desc="计算文档向量"):
            batch_texts = texts[i:i + batch_size]

            # 对文本进行编码
            encoded = self.tokenizer(batch_texts, padding=True, truncation=True,
                                   max_length=512, return_tensors='pt')
            encoded = {k: v.to(self.device) for k, v in encoded.items()}

            # 获取向量表示
            with torch.no_grad():
                outputs = self.model(**encoded)
                # 使用[CLS]标记的输出作为文档表示
                batch_embeddings = outputs.last_hidden_state[:, 0, :].cpu().numpy()

            embeddings.append(batch_embeddings)

        return np.vstack(embeddings)

    def build_index(self):
        """
        构建向量索引
        """
        # 连接数据库
        print("正在连接数据库...")
        conn = pymysql.connect(**self.db_config)
        cursor = conn.cursor()

        try:
            # 查询数据
            print("正在查询数据...")
            cursor.execute("SELECT id, readme FROM repository WHERE readme IS NOT NULL")
            rows = cursor.fetchall()

            if not rows:
                print("没有找到数据")
                return

            # 准备数据
            ids = []
            texts = []
            for row in rows:
                ids.append(row[0])
                texts.append(row[1])

            # 计算文档向量
            print("正在计算文档向量...")
            embeddings = self.get_embeddings(texts)

            # 创建FAISS索引
            print("正在创建向量索引...")
            dimension = embeddings.shape[1]
            index = faiss.IndexFlatL2(dimension)
            index.add(embeddings.astype('float32'))

            # 保存索引
            print("正在保存索引...")
            index_path = os.path.join(self.index_save_path, 'vector_index.faiss')
            faiss.write_index(index, index_path)

            # 保存ID映射
            id_mapping = {i: id_ for i, id_ in enumerate(ids)}
            mapping_path = os.path.join(self.index_save_path, 'id_mapping.json')
            with open(mapping_path, 'w', encoding='utf-8') as f:
                json.dump(id_mapping, f)

            print(f"索引构建完成，共处理 {len(ids)} 条数据")

        finally:
            cursor.close()
            conn.close()

def main():
    # 配置参数
    model_path = r"D:\ModelScope\text2vec-base-chinese"
    db_config = {
        'host': 'localhost',
        'user': 'root',
        'password': 'dd.159178280',
        'database': 'search_engine',
        'charset': 'utf8mb4'
    }
    index_save_path = r"D:\vector_index"

    # 创建索引构建器并执行
    builder = VectorIndexBuilder(model_path, db_config, index_save_path)
    builder.build_index()

if __name__ == "__main__":
    main()