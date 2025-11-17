import { Injectable, NestMiddleware, HttpException, HttpStatus } from '@nestjs/common';
import { Request, Response, NextFunction } from 'express';

interface RateLimitStore {
  [key: string]: {
    count: number;
    resetTime: number;
  };
}

@Injectable()
export class RateLimitMiddleware implements NestMiddleware {
  private store: RateLimitStore = {};
  private readonly limit = 100; // requests per window
  private readonly windowMs = 60 * 1000; // 1 minute

  use(request: Request, response: Response, next: NextFunction): void {
    const key = this.getKey(request);
    const now = Date.now();

    if (!this.store[key] || now > this.store[key].resetTime) {
      this.store[key] = {
        count: 1,
        resetTime: now + this.windowMs,
      };
      return next();
    }

    this.store[key].count++;

    if (this.store[key].count > this.limit) {
      throw new HttpException(
        {
          statusCode: HttpStatus.TOO_MANY_REQUESTS,
          message: 'Too many requests',
          retryAfter: Math.ceil((this.store[key].resetTime - now) / 1000),
        },
        HttpStatus.TOO_MANY_REQUESTS
      );
    }

    // Set rate limit headers
    response.setHeader('X-RateLimit-Limit', this.limit.toString());
    response.setHeader('X-RateLimit-Remaining', (this.limit - this.store[key].count).toString());
    response.setHeader('X-RateLimit-Reset', new Date(this.store[key].resetTime).toISOString());

    next();
  }

  private getKey(request: Request): string {
    // Use IP address and user ID (if authenticated) as key
    const ip = request.ip || request.connection.remoteAddress || 'unknown';
    const userId = (request as any).user?.sub || 'anonymous';
    return `${ip}:${userId}`;
  }

  // Cleanup old entries periodically
  private cleanup(): void {
    const now = Date.now();
    Object.keys(this.store).forEach((key) => {
      if (now > this.store[key].resetTime) {
        delete this.store[key];
      }
    });
  }
}
