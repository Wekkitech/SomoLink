import { Injectable, UnauthorizedException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import * as bcrypt from 'bcrypt';

export interface JwtPayload {
  sub: string;
  username: string;
  email: string;
  role: string;
}

export interface TokenPair {
  accessToken: string;
  refreshToken: string;
}

@Injectable()
export class AuthService {
  constructor(private jwtService: JwtService) {}

  async validateUser(username: string, password: string): Promise<any> {
    // In production, query from database
    // For now, this is a placeholder that validates structure
    
    // Mock user validation
    if (!username || !password) {
      throw new UnauthorizedException('Invalid credentials');
    }

    // This would query the database in production
    const user = await this.findUserByUsername(username);
    
    if (!user) {
      throw new UnauthorizedException('User not found');
    }

    const isPasswordValid = await bcrypt.compare(password, user.password);
    
    if (!isPasswordValid) {
      throw new UnauthorizedException('Invalid password');
    }

    // Remove password from return
    const { password: _, ...result } = user;
    return result;
  }

  async login(user: any): Promise<TokenPair> {
    const payload: JwtPayload = {
      sub: user.id,
      username: user.username,
      email: user.email,
      role: user.role,
    };

    return {
      accessToken: this.jwtService.sign(payload, { expiresIn: '1h' }),
      refreshToken: this.jwtService.sign(payload, { expiresIn: '7d' }),
    };
  }

  async verifyToken(token: string): Promise<JwtPayload> {
    try {
      return this.jwtService.verify(token);
    } catch (error) {
      throw new UnauthorizedException('Invalid or expired token');
    }
  }

  async refreshAccessToken(refreshToken: string): Promise<string> {
    try {
      const payload = this.jwtService.verify(refreshToken);
      const newPayload: JwtPayload = {
        sub: payload.sub,
        username: payload.username,
        email: payload.email,
        role: payload.role,
      };
      return this.jwtService.sign(newPayload, { expiresIn: '1h' });
    } catch (error) {
      throw new UnauthorizedException('Invalid refresh token');
    }
  }

  async hashPassword(password: string): Promise<string> {
    const saltRounds = 10;
    return bcrypt.hash(password, saltRounds);
  }

  private async findUserByUsername(username: string): Promise<any> {
    // Mock user - in production, query from database
    // This is just for structure
    return {
      id: 'mock-user-id',
      username: username,
      email: `${username}@example.com`,
      password: await this.hashPassword('password123'), // Mock password
      role: 'admin',
    };
  }
}
