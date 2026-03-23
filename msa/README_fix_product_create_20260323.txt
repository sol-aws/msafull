수정 내용
1. product S3Config에 명시적 credentials provider 및 timeout 추가
2. ProductService에 S3 업로드/저장 로그 추가
3. ProductService에 AmazonClientException 포함 예외 메시지 반환 추가
4. ProductController create 응답을 JSON으로 명시적 반환
5. CommonExceptionHandler에 Exception 전체 처리 추가

적용 순서
1) msa/product 기준으로 docker build
2) ECR push
3) kubectl rollout restart deployment/product-depl -n soldesk
4) kubectl logs -f deployment/product-depl -n soldesk 로 확인
