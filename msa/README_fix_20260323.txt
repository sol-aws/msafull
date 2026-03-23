수정 반영 내용
1. product application-prod.yml
- 기존 구조 유지
- multipart 설정을 spring 아래로 이동
- DB URL은 기존 흐름대로 ordermsa 유지

2. product controller
- /product/create 응답을 명시적으로 JSON으로 반환
- productId, imageUrl, message 반환

3. product service
- 파일명 공백 치환 로직 수정
- S3 업로드 실패 시 원인 메시지 보이도록 예외 개선

4. ingress
- ingressClassName: nginx 추가

배포 순서
1) msa/product 수정본으로 docker build / push
2) kubectl rollout restart deployment/product-depl -n soldesk
3) 필요 시 apigateway도 재빌드 후 재배포
