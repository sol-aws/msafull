추가/수정 내용

1. 프론트엔드 추가
- msa/frontend
- 메인페이지(index.html)
- 로그인(login.html)
- 회원가입(signup.html)
- 상품등록(product-create.html)

2. 백엔드 수정
- member: 회원가입/로그인 응답 개선, 로그인 로직 예외 수정
- product: 상품 목록 조회 추가, 상품 상세/목록 DTO 확장, category/description/imageUrl 컬럼 추가
- gateway: 공개 경로 판별 로직 보강

3. DB 변경
- product 테이블 컬럼 추가
  category
  description
  image_url(imageUrl)
- ddl-auto를 create -> update로 변경

4. 배포 리소스 추가
- msa/frontend/k8s/depl_svc.yml
- argocd/apps/msa-frontend.yml
- msa/k8s/k8s-services/ingress.yml 수정


수정 사항 추가
- 모든 ECR 이미지 주소를 683668078297.dkr.ecr.ap-northeast-2.amazonaws.com 기준으로 통일했습니다.
- apigateway 정규식 컴파일 오류(\d 이스케이프) 수정했습니다.
- ordering/product deployment 이미지명이 잘못되어 있던 부분을 ordering-service, product-service로 수정했습니다.
- GitHub Actions에 frontend 빌드/배포 단계를 추가했습니다.
