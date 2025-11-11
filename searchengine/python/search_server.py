import os
import json
import torch
import numpy as np
import faiss
from transformers import AutoTokenizer, AutoModel
from http.server import HTTPServer, BaseHTTPRequestHandler
from urllib.parse import parse_qs, urlparse

class VectorSearchServer:
    def __init__(self, model_path, index_path):
        """
        初始化向量搜索服务器
        :param model_path: text2vec模型路径
        :param index_path: 向量索引保存路径
        """
        self.device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
        self.model_path = model_path
        self.index_path = index_path

        # 加载模型和分词器
        print("正在加载模型...")
        self.tokenizer = AutoTokenizer.from_pretrained(model_path)
        self.model = AutoModel.from_pretrained(model_path).to(self.device)
        self.model.eval()

        # 加载向量索引
        print("正在加载向量索引...")
        self.index = faiss.read_index(os.path.join(index_path, 'vector_index.faiss'))

        # 加载ID映射
        with open(os.path.join(index_path, 'id_mapping.json'), 'r', encoding='utf-8') as f:
            self.id_mapping = json.load(f)

    def get_embedding(self, text):
        """
        获取文本的向量表示
        :param text: 输入文本
        :return: 向量数组
        """
        # 对文本进行编码
        encoded = self.tokenizer(text, padding=True, truncation=True,
                               max_length=512, return_tensors='pt')
        encoded = {k: v.to(self.device) for k, v in encoded.items()}

        # 获取向量表示
        with torch.no_grad():
            outputs = self.model(**encoded)
            # 使用[CLS]标记的输出作为文档表示
            embedding = outputs.last_hidden_state[:, 0, :].cpu().numpy()

        return embedding.astype('float32')

    def search(self, query, top_k=2000):
        """
        搜索相似文档
        :param query: 查询文本
        :param top_k: 返回结果数量
        :return: 相似文档ID列表
        """
        # 获取查询向量
        query_vector = self.get_embedding(query)

        # 搜索相似文档
        distances, indices = self.index.search(query_vector, top_k)

        # 转换回原始ID
        results = []
        for idx, distance in zip(indices[0], distances[0]):
            if idx != -1:  # 有效结果
                results.append({
                    'id': self.id_mapping[str(idx)],
                    'score': float(1 / (1 + distance))  # 将距离转换为相似度分数
                })

        return results

class SearchHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        # 解析查询参数
        query_components = parse_qs(urlparse(self.path).query)
        query = query_components.get('q', [''])[0]

        if not query:
            self.send_error(400, "Missing query parameter 'q'")
            return

        try:
            # 执行搜索
            results = self.server.search_server.search(query)

            # 返回结果
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.end_headers()
            self.wfile.write(json.dumps(results, ensure_ascii=False).encode('utf-8'))

        except Exception as e:
            self.send_error(500, str(e))

def run_server(port=8080, model_path="D:/ModelScope/text2vec-base-chinese", index_path="D:/vector_index"):
    # 创建搜索服务器实例
    search_server = VectorSearchServer(model_path, index_path)

    # 创建HTTP服务器
    server = HTTPServer(('localhost', port), SearchHandler)
    server.search_server = search_server

    print(f"Starting server at http://localhost:{port}")
    server.serve_forever()

if __name__ == "__main__":
    run_server()